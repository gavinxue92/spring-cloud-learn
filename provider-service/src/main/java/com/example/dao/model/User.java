package com.example.dao.model;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.util.Date;

@Data
public class User {

    @ExcelProperty("用户ID")
    @ColumnWidth(15)
    private Long id;

    @ExcelProperty("用户名")
    @ColumnWidth(20)
    private String userName;

    @ExcelProperty("密码")
    @ColumnWidth(30)
    private String password;

    @ExcelProperty("电话号码")
    @ColumnWidth(30)
    private String phone;

    @ExcelProperty("邮箱地址")
    @ColumnWidth(30)
    private String email;

    @ExcelProperty("年龄")
    @ColumnWidth(3)
    private Integer age;

    @ExcelProperty("创建时间")
    @ColumnWidth(25)
    private Date createTime;

    @ExcelProperty("状态")
    @ColumnWidth(2)
    private Integer status;
}
