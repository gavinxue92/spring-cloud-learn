package com.example.validate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD) // 仅作用于字段
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateField {

    boolean notNull() default false;             // 是否不能为 null
    boolean notEmpty() default false;            // 字符串或集合是否不能为空串/空集合
    long min() default Long.MIN_VALUE;           // 数值最小值
    long max() default Long.MAX_VALUE;           // 数值最大值
}
