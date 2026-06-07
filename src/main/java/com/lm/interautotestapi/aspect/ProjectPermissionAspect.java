package com.lm.interautotestapi.aspect;

import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lm.interautotestapi.entity.ProjectMember;
import com.lm.interautotestapi.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 项目权限切面
 * 拦截所有 /api/project/{projectId}/... 开头的请求，校验当前登录用户是否是项目成员
 * 从请求 URI 中提取 projectId，不依赖参数顺序
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ProjectPermissionAspect {

    private static final Pattern PROJECT_ID_PATTERN = Pattern.compile("/api/project/(\\d+)/");

    private final ProjectMemberService projectMemberService;

    @Around("execution(* com.lm.interautotestapi.controller.*.*(..)) && " +
            "(@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping))")
    public Object checkProjectMember(ProceedingJoinPoint pjp) throws Throwable {
        // 获取当前请求
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = Objects.requireNonNull(attrs).getRequest();

        String uri = request.getRequestURI();

        // 只拦截 /api/project/{projectId}/... 格式的路径
        Matcher matcher = PROJECT_ID_PATTERN.matcher(uri);
        if (!matcher.find()) {
            return pjp.proceed();
        }

        Long projectId = Long.parseLong(matcher.group(1));

        // 排除 /api/project/{projectId}/open/health（无需登录）
        if (uri.endsWith("/open/health")) {
            return pjp.proceed();
        }

        // 检查登录状态
        if (!StpUtil.isLogin()) {
            throw new NotPermissionException("未登录，请先登录");
        }

        Long userId = StpUtil.getLoginIdAsLong();

        // 项目成员管理特殊处理：项目 OWNER/ADMIN 可以管理成员
        // 如果是 /api/project/{projectId}/members/**，OWNER/ADMIN 始终有权限
        boolean isMemberManagement = uri.contains("/members");

        // 校验成员资格
        ProjectMember membership = projectMemberService.getOne(
                new LambdaQueryWrapper<ProjectMember>()
                        .eq(ProjectMember::getProjectId, projectId)
                        .eq(ProjectMember::getUserId, userId));

        if (membership == null) {
            // 特殊放行：用户管理项目列表（/api/project/* 不带额外路径的）
            // 这种已经在 /api/project/page 和 /api/project/list 中处理
            log.warn("[ProjectPermission] 权限拒绝: userId={} 不属于 projectId={}, uri={}", userId, projectId, uri);
            throw new NotPermissionException("无权访问该项目，请确认您是项目成员");
        }

        return pjp.proceed();
    }
}
