package com.example.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.example.dao.model.User;
import com.example.service.UserService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class UserDataListener implements ReadListener<User> {

    /**
     * 每隔 100 条批量入库一次，防止内存溢出 (OOM)
     */
    private static final int BATCH_COUNT = 1000;
    private List<User> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    private final UserService userService;
    private final ThreadPoolExecutor threadPoolExecutor;

    public UserDataListener(UserService userService, ThreadPoolExecutor threadPoolExecutor) {
        this.userService = userService;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    // 用于异步任务追踪，确保全部入库完成
    private List<CompletableFuture<Void>> futures = ListUtils.newArrayList();

    /**
     * 每读取一行都会调用此方法
     * @param data    one row value. It is same as {@link AnalysisContext#readRowHolder()}
     * @param context analysis context
     */
    @Override
    public void invoke(User data, AnalysisContext context) {
        // 数据不存在加入
//        if (validateData(data)) {
            cachedDataList.add(data);
//        }
        // 达到批次大小，触发异步入库
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveDataAsync();
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    /**
     * 所有数据读取完后调用此方法
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 处理最后剩余不足 1000 条的数据
        if (!cachedDataList.isEmpty()) {
            saveDataAsync();
        }

        // 阻塞等待所有异步线程的数据库操作全部执行完毕
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("全部 Excel 数据解析并多线程异步入库完成！");
    }

//    private boolean validateData(User user) {
//        int count = userService.countById(user.getId());
//        // count为0表示数据不存在
//        if (count == 0) {
//            return true;
//        }
//        return false;
//    }

    /**
     * 异步提交到线程池执行批量入库
     */
    private void saveDataAsync() {
        // 必须对当前批次列表进行拷贝，防止多线程并发修改同一个 List 引用
        List<User> batchData = ListUtils.newArrayList(cachedDataList);

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            long start = System.currentTimeMillis();
            userService.batchInser(batchData);
            log.info("线程 [{}] 成功批量插入 {} 条数据，耗时：{} ms",
                    Thread.currentThread().getName(), batchData.size(), (System.currentTimeMillis() - start));
        }, threadPoolExecutor);

        futures.add(future);
    }
}
