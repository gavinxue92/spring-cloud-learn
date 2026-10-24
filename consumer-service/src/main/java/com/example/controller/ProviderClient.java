package com.example.controller;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// value 指定注册中心里的服务提供者名称，Feign 会自动做负载均衡并完美传递 traceId
@FeignClient(value = "provider-service")
public interface ProviderClient {

    @GetMapping("/hello/{name}")
    String getHelloFromProvider(@PathVariable("name") String name);
}
