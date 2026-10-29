package com.example.mq;

import com.example.dto.UserDto;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
        topic = "My-Obj-Topic",           // 监听的 Topic，需与发送方一致
        consumerGroup = "my-consumer-group-obj" // 消费者组名（唯一）
)
public class RocketMqObjectConsumer implements RocketMQListener<UserDto> {

    @Override
    public void onMessage(UserDto userDto) {
        System.out.println("<<< [RocketMQ] 成功消费对象消息: " + userDto.getUserName());
    }
}
