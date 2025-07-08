package com.codegym.kanflow.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    /**
     * Pointcut này sẽ cố gắng bắt các phương thức trong package "service.impl"
     * (Giả định rằng bạn có cấu trúc thư mục service/impl)
     */
    @AfterReturning(pointcut = "execution(* com.codegym.kanflow.service.impl.*.*(..))", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String args = Arrays.toString(joinPoint.getArgs());

        System.out.printf("[AOP LOG - TEST LỖI] %s - Method executed: %s.%s() | Args: %s | Result: %s\n",
                LocalDateTime.now(), className, methodName, args, result);
    }
}