package com.example.mq;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RocketMqProducerService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;


    /**
     * 发送普通文本消息
     */
    public void sendMsg(String message) {
        rocketMQTemplate.convertAndSend("My-String-Topic", message);
        System.out.println(">>> [RocketMQ] 发送消息成功: " + message);
    }

    /**
     * 发送对象消息（会自动转成 JSON）
     */
    public void sendUserObj(Object userObj) {
        rocketMQTemplate.convertAndSend("My-Obj-Topic", userObj);
        System.out.println(">>> [RocketMQ] 发送对象消息成功: " + userObj);
    }
}
