package com.example.validate;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import java.lang.reflect.Field;
import java.util.Collection;

@Slf4j
@Aspect
@Component
public class ParamValidateAspect {

    @Around("@annotation(paramValidate)")
    public Object doValidate(ProceedingJoinPoint joinPoint, ParamValidate paramValidate) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args == null) {
            return joinPoint.proceed();
        }

        // 遍历方法的所有入参
        for (Object arg : args) {
            if (arg != null) {
                // 对入参对象内部带有 @ValidateField 的字段进行校验
                validateObjectFields(arg);
            }
        }
        return joinPoint.proceed();
    }

    /**
     * 通过反射递归或逐层扫描对象的字段注解
     */
    private void validateObjectFields(Object obj) throws IllegalAccessException {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true); // 允许访问私有字段

            if (field.isAnnotationPresent(ValidateField.class)) {
                ValidateField rule = field.getAnnotation(ValidateField.class);
                Object value = field.get(obj);
                String fieldName = field.getName();

                // 1. NotNull 校验
                if (rule.notNull() && value == null) {
                    throw new IllegalArgumentException("字段 [" + fieldName + "] 校验失败: ");
                }

                if (value != null) {
                    // 2. NotEmpty 校验 (针对 String 或 Collection)
                    if (rule.notEmpty()) {
                        if (value instanceof String && ((String) value).trim().isEmpty()) {
                            throw new IllegalArgumentException("字段 [" + fieldName + "] 校验失败: ");
                        }
                        if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
                            throw new IllegalArgumentException("字段 [" + fieldName + "] 校验失败: ");
                        }
                    }

                    // 3. 数值范围校验 (min / max)
                    if (value instanceof Number) {
                        long numVal = ((Number) value).longValue();
                        if (numVal < rule.min() || numVal > rule.max()) {
                            throw new IllegalArgumentException("字段 [" + fieldName + "] 数值超出范围 [" + rule.min() + " ~ " + rule.max() + "]: ");
                        }
                    }
                }
            }
        }
    }

}
