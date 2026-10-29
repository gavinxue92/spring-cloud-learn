package com.example.mq;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    private static final String TRANSACTION_TOPIC = "My-Transaction-Topic";

    @Transactional(rollbackFor = Exception.class) // Spring 本地事务
    public void createOrderWithTransaction(String orderInfo) {
        // 1. 构建 Spring Messaging 的 Message 对象
        Message<String> msg = MessageBuilder.withPayload(orderInfo).build();

        /**
         * 2. 发送事务消息 (半消息)
         * 参数说明：
         *   - destination: Topic名称
         *   - message: 消息对象
         *   - arg: 传递给本地事务的扩展参数（可选）
         */

        rocketMQTemplate.sendMessageInTransaction(TRANSACTION_TOPIC, msg, null);

        System.out.println(">>> [Producer] 事务半消息发送触发成功: " + orderInfo);
    }
}
