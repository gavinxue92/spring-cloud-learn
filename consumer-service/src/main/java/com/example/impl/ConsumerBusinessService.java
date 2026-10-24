package com.example.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.example.controller.ProviderClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConsumerBusinessService {

    @Autowired
    private ProviderClient providerClient;

    /**
     * value: 资源名称（可在 Sentinel 控制台针对该资源配置 QPS 限流或熔断规则）
     * blockHandler: 触发 Sentinel 限流/流控时的处理方法
     * fallback: 业务代码抛出异常时的处理方法
     */
    @SentinelResource(value = "consumeHelloResource",
            blockHandler = "handleBlock",
            fallback = "handleFallback")
    public String callProviderService(String name) {
        // 这里发起 OpenFeign 调用
        return providerClient.getHelloFromProvider(name);
    }

    /**
     * Sentinel 限流兜底方法
     * （参数必须与原方法一致，最后可追加 BlockException）
     */
    public String handleBlock(String name, BlockException ex) {
        return "【Sentinel 限流】当前访问人数过多，触发流控保护，请稍后再试！";
    }

    /**
     * Sentinel 异常熔断兜底方法
     * （参数必须与原方法一致，最后可追加 Throwable）
     */
    public String handleFallback(String name, Throwable t) {
        return "【Sentinel 降级】系统开小差了：" + t.getMessage();
    }
}
