package com.example.controller;

import com.example.common.Result;
import com.example.dto.UserDto;
import com.example.validate.ParamValidate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProviderController {

    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/hello/{name}")
    public String hello(@PathVariable("name") String name) {
        return "Hello " + name + ", response from provider, port: " + serverPort;
    }

    @ParamValidate
    @PostMapping("/testValidateParam")
    public Result<String> testValidateParam(@RequestBody UserDto userDto) {
        return Result.success();
    }
}
