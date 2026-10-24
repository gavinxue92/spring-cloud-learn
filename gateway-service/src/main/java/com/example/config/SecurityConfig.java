package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // 禁用 CSRF 保护
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable) // 禁用默认的 /login 表单登录页面
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable) // 禁用 Http Basic 认证
                .authorizeExchange(exchange -> exchange
                        .anyExchange().permitAll() // 允许所有请求通过 Spring Security 这一层，由我们自己的 AuthorizeFilter 进行精确的 JWT 校验与 401 拦截
                )
                .build();
    }
}
