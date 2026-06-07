package com.lm.interautotestapi.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lm.interautotestapi.common.Result;
import com.lm.interautotestapi.entity.*;
import com.lm.interautotestapi.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectMemberService projectMemberService;
    private final SysUserService sysUserService;
    private final ApiInterfaceService apiInterfaceService;
    private final ApiTestcaseService apiTestcaseService;

    @GetMapping("/page")
    @SaCheckPermission("user:manage")
    public Result<Page<Project>> page(@RequestParam(defaultValue = "1") int pageNum,
                                      @RequestParam(defaultValue = "10") int pageSize,
                                      @RequestParam(required = false) String keyword) {
        Page<Project> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Project::getProjectName, keyword)
                    .or().like(Project::getProjectCode, keyword);
        }
        wrapper.orderByDesc(Project::getId);
        Page<Project> result = projectService.page(page, wrapper);

        if (!result.getRecords().isEmpty()) {
            Set<Long> ownerIds = result.getRecords().stream()
                    .map(Project::getOwnerId).filter(Objects::nonNull).collect(Collectors.toSet());
            Map<Long, String> ownerNameMap = new HashMap<>();
            if (!ownerIds.isEmpty()) {
                sysUserService.listByIds(ownerIds).forEach(u -> ownerNameMap.put(u.getId(), u.getNickname() != null ? u.getNickname() : u.getUsername()));
            }

            List<Long> projectIds = result.getRecords().stream().map(Project::getId).collect(Collectors.toList());
            Map<Long, Long> memberCountMap = new HashMap<>();
            Map<Long, Long> ifaceCountMap = new HashMap<>();
            if (!projectIds.isEmpty()) {
                List<ProjectMember> allMembers = projectMemberService.list(
                        new LambdaQueryWrapper<ProjectMember>().in(ProjectMember::getProjectId, projectIds));
                allMembers.forEach(m -> memberCountMap.merge(m.getProjectId(), 1L, Long::sum));

                List<ApiInterface> allIfaces = apiInterfaceService.list(
                        new LambdaQueryWrapper<ApiInterface>().in(ApiInterface::getProjectId, projectIds));
                allIfaces.forEach(i -> ifaceCountMap.merge(i.getProjectId(), 1L, Long::sum));
            }

            for (Project p : result.getRecords()) {
                p.setOwnerName(ownerNameMap.getOrDefault(p.getOwnerId(), "-"));
                p.setMemberCount(memberCountMap.getOrDefault(p.getId(), 0L).intValue());
                p.setInterfaceCount(ifaceCountMap.getOrDefault(p.getId(), 0L).intValue());
            }
        }

        return Result.ok(result);
    }

    @GetMapping("/{id}")
    public Result<Project> getById(@PathVariable Long id) {
        Project project = projectService.getById(id);
        if (project != null && project.getOwnerId() != null) {
            SysUser owner = sysUserService.getById(project.getOwnerId());
            if (owner != null) {
                project.setOwnerName(owner.getNickname() != null ? owner.getNickname() : owner.getUsername());
            }
        }
        return Result.ok(project);
    }

    @GetMapping("/list")
    public Result<List<Project>> listAll() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<ProjectMember> memberships = projectMemberService.list(
                new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getUserId, userId));
        if (memberships.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        List<Long> projectIds = memberships.stream().map(ProjectMember::getProjectId).collect(Collectors.toList());
        List<Project> projects = projectService.list(
                new LambdaQueryWrapper<Project>().in(Project::getId, projectIds).eq(Project::getStatus, 1));
        return Result.ok(projects);
    }

    @PostMapping
    @SaCheckPermission("user:manage")
    @Transactional(rollbackFor = Exception.class)
    public Result<Project> save(@RequestBody Project project) {
        projectService.save(project);

        ProjectMember pm = new ProjectMember();
        pm.setProjectId(project.getId());
        pm.setUserId(project.getOwnerId() != null ? project.getOwnerId() : StpUtil.getLoginIdAsLong());
        pm.setRole("OWNER");
        projectMemberService.save(pm);

        return Result.ok(project);
    }

    @PutMapping
    @SaCheckPermission("user:manage")
    public Result<Void> update(@RequestBody Project project) {
        projectService.updateById(project);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("user:manage")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(@PathVariable Long id) {
        projectMemberService.remove(new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getProjectId, id));
        apiTestcaseService.remove(new LambdaQueryWrapper<ApiTestcase>().eq(ApiTestcase::getProjectId, id));
        apiInterfaceService.remove(new LambdaQueryWrapper<ApiInterface>().eq(ApiInterface::getProjectId, id));
        projectService.removeById(id);
        return Result.ok();
    }

    @GetMapping("/{projectId}/members")
    public Result<List<ProjectMember>> members(@PathVariable Long projectId) {
        List<ProjectMember> members = projectMemberService.list(
                new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getProjectId, projectId));
        if (!members.isEmpty()) {
            Set<Long> userIds = members.stream().map(ProjectMember::getUserId).collect(Collectors.toSet());
            Map<Long, SysUser> userMap = sysUserService.listByIds(userIds).stream()
                    .collect(Collectors.toMap(SysUser::getId, u -> u));
            members.forEach(m -> {
                SysUser u = userMap.get(m.getUserId());
                if (u != null) {
                    m.setUsername(u.getUsername());
                    m.setNickname(u.getNickname());
                }
            });
        }
        return Result.ok(members);
    }

    @PostMapping("/{projectId}/members")
    @SaCheckPermission("user:manage")
    public Result<Void> addMembers(@PathVariable Long projectId, @RequestBody List<Map<String, Object>> body) {
        for (Map<String, Object> item : body) {
            Long userId = item.get("userId") instanceof Integer ? ((Integer) item.get("userId")).longValue() : (Long) item.get("userId");
            String role = (String) item.getOrDefault("role", "MEMBER");
            ProjectMember existing = projectMemberService.getOne(
                    new LambdaQueryWrapper<ProjectMember>()
                            .eq(ProjectMember::getProjectId, projectId)
                            .eq(ProjectMember::getUserId, userId));
            if (existing == null) {
                ProjectMember pm = new ProjectMember();
                pm.setProjectId(projectId);
                pm.setUserId(userId);
                pm.setRole(role);
                projectMemberService.save(pm);
            }
        }
        return Result.ok();
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    @SaCheckPermission("user:manage")
    public Result<Void> removeMember(@PathVariable Long projectId, @PathVariable Long userId) {
        projectMemberService.remove(
                new LambdaQueryWrapper<ProjectMember>()
                        .eq(ProjectMember::getProjectId, projectId)
                        .eq(ProjectMember::getUserId, userId));
        return Result.ok();
    }
}
