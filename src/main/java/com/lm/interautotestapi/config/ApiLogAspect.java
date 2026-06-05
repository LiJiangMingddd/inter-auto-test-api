package com.lm.interautotestapi.config;

import cn.hutool.json.JSONUtil;
import com.lm.interautotestapi.common.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * API 接口日志切面 —— 自动记录所有 Controller 的请求入参和响应结果
 */
@Aspect
@Component
public class ApiLogAspect {

    /**
     * 切点：所有 RestController 下的所有方法
     */
    @Pointcut("execution(public * com.lm.interautotestapi.controller.*.*(..))")
    public void apiPointcut() {}

    @Around("apiPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取 Logger（按目标类名）
        Logger log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());

        // 获取请求信息
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs != null ? attrs.getRequest() : null;

        // 方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getMethod().getName();

        // ----- 请求日志 -----
        String httpMethod = request != null ? request.getMethod() : "-";
        String requestUri = request != null ? request.getRequestURI() : "-";
        String queryString = request != null ? request.getQueryString() : null;
        String params = joinPoint.getArgs() != null
                ? Arrays.stream(joinPoint.getArgs())
                .map(arg -> {
                    if (arg == null) return "null";
                    String str = arg.toString();
                    // 太长则截断
                    return str.length() > 500 ? str.substring(0, 500) + "...(truncated)" : str;
                })
                .collect(Collectors.joining(", "))
                : "";

        log.info("▶▶▶ [请求] {} {} | {} | 参数: {}",
                httpMethod, requestUri,
                (queryString != null ? "?" + queryString : ""),
                params.isEmpty() ? "无" : params);

        long startTime = System.currentTimeMillis();

        // 执行目标方法
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            log.error("✘✘✘ [异常] {} | {} - {}", methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }

        long elapsed = System.currentTimeMillis() - startTime;

        // ----- 响应日志 -----
        if (result instanceof Result) {
            Result<?> r = (Result<?>) result;
            log.info("◀◀◀ [响应] {} | code={} msg={} | 耗时={}ms",
                    methodName, r.getCode(), r.getMsg(), elapsed);
        } else {
            String respStr = JSONUtil.toJsonStr(result);
            respStr = respStr.length() > 500 ? respStr.substring(0, 500) + "...(truncated)" : respStr;
            log.info("◀◀◀ [响应] {} | data={} | 耗时={}ms", methodName, respStr, elapsed);
        }

        return result;
    }
}
