package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {

    @Bean("excelImportThreadPool")
    public ThreadPoolExecutor excelImportThreadPool() {
        int cpuCore = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(
                cpuCore * 2,                                // 核心线程数
                cpuCore * 4,                                // 最大线程数
                60L, TimeUnit.SECONDS,                      // 空闲线程存活时间
                new ArrayBlockingQueue<>(2000),             // 有界阻塞队列，防止积压过多
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()   // 队列满了由调用线程处理，防止拒绝策略抛异常
        );
    }
}
