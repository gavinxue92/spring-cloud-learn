package com.example.controller;

import com.example.impl.ConsumerBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RefreshScope // 关键注解：开启配置自动更新能力，当 Nacos 配置改变时，Bean 会重新加载
public class ConsumerController {

    @Autowired
    private ConsumerBusinessService consumerBusinessService; // 注入带有 Sentinel 保护的 Service

    // 从 Nacos 配置中心读取 pattern.welcomes 的值
    @Value("${pattern.welcomes:default_welcome}")
    private String welcomeMsg;

    @GetMapping("/consumer/hello/{name}")
    public String consumeHello(@PathVariable("name") String name) {
        // 直接调用，底层会自动轮询多个实例，并且 Zipkin 链路 100% 完美合一
        String providerResult = consumerBusinessService.callProviderService(name);

        // 拼接从 Nacos 读取的欢迎语
        return welcomeMsg + " | " + providerResult;
    }
}
