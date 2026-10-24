package com.example.filter;

import com.example.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 放行登录、注册等公开接口（假设 /auth/login 不需要认证）
        Boolean flag = true;
        if (path.contains("/auth/login") || flag == true) {
            return chain.filter(exchange);
        }

        // 2. 获取请求头中的 Authorization
        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Claims claims = jwtUtils.parseToken(token);
            if (claims != null) {
                // Token 合法，放行并通过请求头将用户信息透传给下游
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("username", claims.getSubject())
                        .build();
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            }
        }

        // 3. 校验失败，拦截并返回 401 Unauthorized
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return 0; // 优先级，数字越小越先执行
    }
}
