package com.example.wallet.aspect;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(public * com.example.wallet.service..*(..)) || " +
            "execution(public * com.example.wallet.coincap.service..*(..))")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        String methodId = String.format("[%s#%s]", className, methodName);
        if (args.length > 0) {
            log.info("{} Invocado com argumentos: {}", methodId, Arrays.toString(args));
        } else {
            log.info("{} Invocado", methodId);
        }

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - startTime;
            log.info("{} Sucesso em {}ms", methodId, duration);

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("{} ERROR after {}ms: {} - {}", methodId, duration, e.getClass().getSimpleName(),
                    e.getMessage(), e);
            throw e;
        }
    }

}
