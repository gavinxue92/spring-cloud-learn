package com.example.mq;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
        topic = "My-String-Topic",           // 监听的 Topic，需与发送方一致
        consumerGroup = "my-consumer-group-string" // 消费者组名（唯一）
)
public class RocketMqConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        // 收到消息后的业务处理逻辑
        System.out.println("<<< [RocketMQ] 成功消费消息: " + message);
    }
}
