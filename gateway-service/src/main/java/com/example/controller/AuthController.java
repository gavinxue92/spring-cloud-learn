package com.example.controller;

import com.example.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private JwtUtils jwtUtils;

    // 模拟登录接口（路径为 /auth/login，会自动被前面写的过滤器放行）
    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String username, @RequestParam String password) {
        Map<String, Object> result = new HashMap<>();

        // 简单模拟校验（实际项目中应查询数据库比对密码）
        if ("admin".equals(username) && "123456".equals(password)) {
            // 登录成功，生成 JWT Token
            String token = jwtUtils.generateToken(username);
            result.put("code", 200);
            result.put("message", "登录成功！");
            result.put("token", "Bearer " + token); // 规范格式：Bearer <token>
        } else {
            result.put("code", 400);
            result.put("message", "用户名或密码错误！");
        }
        return result;
    }
}
