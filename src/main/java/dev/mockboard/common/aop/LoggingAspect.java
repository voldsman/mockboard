package dev.mockboard.common.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut(
            "within(@org.springframework.stereotype.Repository *)" +
                    " || within(@org.springframework.stereotype.Service *)" +
                    " || within(@org.springframework.web.bind.annotation.RestController *)"
    )
    public void springBeanPointcut() {
        // noop
    }

    @Pointcut(
            "within(dev.mockboard.web..*)" +
                    " || within(dev.mockboard.service..*)" +
                    " || within(dev.mockboard.storage..*)"
    )
    public void applicationPackagePointcut() {
        // noop
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerPointcut() {
        // noop
    }

    @AfterThrowing(pointcut = "applicationPackagePointcut() && springBeanPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        log.error(
                "Exception in {}() with cause = '{}' and exception = '{}'",
                joinPoint.getSignature().toShortString(),
                e.getCause() != null ? e.getCause() : "NULL",
                e.getMessage(),
                e
        );
    }

    @Around("applicationPackagePointcut() && springBeanPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug(
                    "Enter: {}() with argument[s] = {}",
                    joinPoint.getSignature().toShortString(),
                    Arrays.toString(joinPoint.getArgs())
            );
        }

        try {
            var result = joinPoint.proceed();
            if (log.isDebugEnabled()) {
                log.debug(
                        "Exit: {}() with result = {}",
                        joinPoint.getSignature().toShortString(),
                        result
                );
            }
            return result;
        } catch (IllegalArgumentException e) {
            log.error(
                    "Illegal argument: {} in {}()",
                    Arrays.toString(joinPoint.getArgs()),
                    joinPoint.getSignature().toShortString()
            );
            throw e;
        }
    }

    @Around("restControllerPointcut()")
    public Object logRestController(ProceedingJoinPoint joinPoint) throws Throwable {
        var request = getCurrentRequest();
        if (request != null && log.isInfoEnabled()) {
            log.info(
                    "REST Request: {} {} - Method: {}()",
                    request.getMethod(),
                    request.getRequestURI(),
                    joinPoint.getSignature().toShortString()
            );
        }

        long startTime = System.currentTimeMillis();
        try {
            var result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            if (request != null && log.isInfoEnabled()) {
                log.info(
                        "REST Response: {} {} - Status: SUCCESS - Time: {}ms",
                        request.getMethod(),
                        request.getRequestURI(),
                        executionTime
                );
            }

            return result;
        } catch (Throwable e) {
            long executionTime = System.currentTimeMillis() - startTime;

            if (request != null) {
                log.error(
                        "REST Response: {} {} - Status: ERROR - Time: {}ms - Exception: {}",
                        request.getMethod(),
                        request.getRequestURI(),
                        executionTime,
                        e.getMessage()
                );
            }

            throw e;
        }
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
