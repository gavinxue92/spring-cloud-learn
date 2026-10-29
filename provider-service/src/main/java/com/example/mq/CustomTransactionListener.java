package com.example.mq;

import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@RocketMQTransactionListener
public class CustomTransactionListener implements RocketMQLocalTransactionListener {
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        System.out.println(">>> [事务监听] 开始执行本地事务...");
        try {
            // 1. 获取传递进来的业务参数或消息内容 UserDto userDto = (UserDto) msg.getPayload
            String payload = new String((byte[]) msg.getPayload());

            // 2. 执行你的本地数据库事务（例如：保存订单、扣减库存等）
            // boolean success = orderService.doLocalTransaction(payload);
            Thread.sleep(2000);
            boolean success = true; // 假设本地事务执行成功

            if (success) {
                System.out.println(">>> [事务监听] 本地事务执行成功，提交消息");
                return RocketMQLocalTransactionState.COMMIT; // 提交消息，消费者可见
            } else {
                System.out.println(">>> [事务监听] 本地事务执行失败，回滚消息");
                return RocketMQLocalTransactionState.ROLLBACK; // 回滚消息，消费者不可见
            }
        } catch (Exception e) {
            System.err.println(">>> [事务监听] 本地事务异常，回滚消息: " + e.getMessage());
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        System.out.println(">>> [事务回查] Broker 正在回查本地事务状态...");
        try {
            // 3. 当本地事务没有返回明确结果时，Broker 会定时回查数据库或日志
            // 检查该笔业务在本地数据库是否真正执行成功
            // boolean isCommitted = orderDao.checkExists(msgId);

            boolean isCommitted = true; // 假设根据业务校验已成功

            if (isCommitted) {
                return RocketMQLocalTransactionState.COMMIT;
            } else {
                return RocketMQLocalTransactionState.ROLLBACK;
            }
        } catch (Exception e) {
            return RocketMQLocalTransactionState.UNKNOWN; // 无法确定继续回查
        }
    }

}
