package com.example.dto;
import com.example.validate.ValidateField;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class UserDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ValidateField(notNull = true)
    private Long id;

    @ValidateField(min = 2)
    private String userName;

    private String password;

    private String phone;

    private String email;

    private Integer age;

    private Date createTime;

    private Integer status;
}
