package com.zookeeper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
public class ZookeeperFairLock {

    private final CuratorFramework client;
    private final String lockName;
    private static final String ROOT_LOCK_PATH = "/locks";

    // 当前客户端持有的锁节点完整路径（例如：/locks/my_lock-000000001）
    private String currentLockPath;
    // 等待前一个节点释放的同步计数器
    private CountDownLatch waitLatch;
    private final AtomicBoolean isLocked = new AtomicBoolean(false);

    /**
     * 构造函数：初始化 Curator 客户端
     * @param connectString ZooKeeper 连接地址，例如 "127.0.0.1:2181"
     * @param lockName      锁名称（对应根节点下的子锁名）
     */
    public ZookeeperFairLock(String connectString, String lockName) {
        this.lockName = lockName;
        // 生产级重试策略：初始休眠1秒，重试3次
        this.client = CuratorFrameworkFactory.newClient(
                connectString,
                5000,
                3000,
                new ExponentialBackoffRetry(1000, 3)
        );
        this.client.start();
    }

    /**
     * 尝试获取公平锁（阻塞式）
     */
    public boolean lock(long timeout, TimeUnit unit) {
        try {
            // 1. 确保根节点存在
            if (client.checkExists().forPath(ROOT_LOCK_PATH) == null) {
                try {
                    client.create().creatingParentContainersIfNeeded().forPath(ROOT_LOCK_PATH);
                } catch (KeeperException.NodeExistsException e) {
                    // 忽略并发创建冲突
                }
            }

            // 2. 在根节点下创建临时有序节点（前缀为 lockName + "-"）
            String baseNodePath = ROOT_LOCK_PATH + "/" + lockName + "-";
            currentLockPath = client.create()
                    .creatingParentContainersIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(baseNodePath);

            System.out.println("[" + Thread.currentThread().getName() + "] 创建节点成功: " + currentLockPath);

            // 3. 循环尝试获取锁或等待前一个节点
            long millisTimeout = unit.toMillis(timeout);
            long deadline = System.currentTimeMillis() + millisTimeout;

            while (true) {
                if (tryLockOnce()) {
                    isLocked.set(true);
                    return true;
                }

                // 超时检查
                long timeLeft = deadline - System.currentTimeMillis();
                if (timeLeft <= 0) {
                    // 超时未获取到锁，清理自己创建的节点
                    unlock();
                    return false;
                }

                // 阻塞等待前一个节点释放（通过 CountDownLatch）
                if (waitLatch != null) {
                    boolean acquired = waitLatch.await(timeLeft, TimeUnit.MILLISECONDS);
                    if (!acquired) {
                        unlock();
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取分布式锁异常", e);
        }
    }

    /**
     * 单次尝试加锁核心逻辑
     */
    private boolean tryLockOnce() throws Exception {
        // 获取根节点下的所有子节点
        List<String> children = client.getChildren().forPath(ROOT_LOCK_PATH);
        // 排序
        Collections.sort(children);

        // 获取当前自己节点的名称（去掉路径前缀）
        String selfNodeName = currentLockPath.substring(currentLockPath.lastIndexOf("/") + 1);
        int selfIndex = children.indexOf(selfNodeName);

        if (selfIndex < 0) {
            throw new IllegalStateException("当前锁节点异常丢失: " + currentLockPath);
        }

        // 如果自己排在第一个，说明加锁成功
        if (selfIndex == 0) {
            System.out.println("[" + Thread.currentThread().getName() + "] 恭喜！成功获取锁: " + currentLockPath);
            return true;
        }

        // 如果不是第一个，监听排在自己正前方的那个节点
        String prevNodeName = children.get(selfIndex - 1);
        String prevNodePath = ROOT_LOCK_PATH + "/" + prevNodeName;

        waitLatch = new CountDownLatch(1);

        // 使用 stat().watched().forPath() 监控前一个节点
        StatResult stat = watchNode(prevNodePath);
        if (!stat.exists) {
            // 如果前一个节点刚好在监听前消失了，则直接进入下一轮循环重试
            return tryLockOnce();
        }

        return false;
    }

    /**
     * 监控指定节点，节点被删除时触发回调
     */
    private StatResult watchNode(String path) throws Exception {
        StatResult result = new StatResult();
        // 注册 Watcher，当节点被删除（NodeDeleted）时唤醒 CountDownLatch
        byte[] data = client.getData().usingWatcher((Watcher) event -> {
            if (event.getType() == Watcher.Event.EventType.NodeDeleted) {
                System.out.println("[" + Thread.currentThread().getName() + "] 监听到前置节点释放，尝试抢锁...");
                if (waitLatch != null) {
                    waitLatch.countDown();
                }
            }
        }).inBackground().forPath(path); // 结合后台异步或直接读取

        // 简化的同步监听写法
        result.exists = (client.checkExists().usingWatcher((Watcher) event -> {
            if (event.getType() == Watcher.Event.EventType.NodeDeleted) {
                if (waitLatch != null) {
                    waitLatch.countDown();
                }
            }
        }).forPath(path) != null);

        return result;
    }

    private static class StatResult {
        boolean exists;
    }

    /**
     * 释放锁
     */
    public void unlock() {
        try {
            if (isLocked.compareAndSet(true, false) || currentLockPath != null) {
                if (client.checkExists().forPath(currentLockPath) != null) {
                    client.delete().forPath(currentLockPath);
                    System.out.println("[" + Thread.currentThread().getName() + "] 释放锁成功，已删除节点: " + currentLockPath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (Exception ignored) {}
        }
    }

    // ==================== 测试验证主函数 ====================
    public static void main(String[] args) throws InterruptedException {
        String zkServer = "127.0.0.1:2181"; // 本机已启动的 ZooKeeper 地址
        int threadCount = 3;
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        for (int i = 1; i <= threadCount; i++) {
            final int clientId = i;
            new Thread(() -> {
                ZookeeperFairLock lock = new ZookeeperFairLock(zkServer, "business_order_lock");
                try {
                    System.out.println("客户端 " + clientId + " 正在尝试获取锁...");
                    // 尝试加锁，最多等待 10 秒
                    if (lock.lock(10, TimeUnit.SECONDS)) {
                        System.out.println("===> 客户端 " + clientId + " 获得锁，开始执行核心业务逻辑...");
                        Thread.sleep(2000); // 模拟业务耗时 2 秒
                    } else {
                        System.out.println("客户端 " + clientId + " 获取锁超时。");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                    finishLatch.countDown();
                }
            }, "Client-Thread-" + clientId).start();
        }

        finishLatch.await();
        System.out.println("所有客户端测试任务执行完毕。");
    }
}
