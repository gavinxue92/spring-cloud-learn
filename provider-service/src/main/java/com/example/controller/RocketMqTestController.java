package com.example.controller;

import com.example.common.Result;
import com.example.dto.UserDto;
import com.example.mq.OrderService;
import com.example.mq.RocketMqProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class RocketMqTestController {

    @Autowired
    private RocketMqProducerService producerService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/sendRocketMsg")
    public Result<String> sendRocketMsg(@RequestParam("msg") String msg) {
        producerService.sendMsg(msg);
        return Result.success("消息发送触发成功: " + msg);
    }

    @PostMapping("/sendRocketObj")
    public Result<String> sendRocketObj(@RequestBody UserDto userDto) {
        producerService.sendUserObj(userDto);
        return Result.success("消息发送触发成功: " + userDto);
    }

    @GetMapping("/sendRocketTransaction")
    public Result<String> sendRocketTransaction(@RequestParam("msg") String msg) {
        orderService.createOrderWithTransaction(msg);
        return Result.success("消息发送触发成功: " + msg);
    }
}
