# 接口自动化中台

> **作者：lijiangming**
>
> 一站式接口自动化测试管理平台，提供接口管理、用例管理、项目隔离、在线测试、数据统计及对外 Open API 能力。
> 基于 Spring Boot + Sa-Token + MyBatis-Plus 构建，前端采用 Thymeleaf + Layui + ECharts + Font Awesome。

---

## 目录

- [项目简介](#项目简介)
- [技术架构](#技术架构)
- [核心功能](#核心功能)
- [快速启动](#快速启动)
- [项目结构](#项目结构)
- [项目隔离模型](#项目隔离模型)
- [认证体系](#认证体系)
- [核心 API](#核心-api)
- [Open API 调用指南](#open-api-调用指南)
- [数据库设计](#数据库设计)
- [配置说明](#配置说明)
- [用例格式指南](#用例格式指南)
- [常见问题](#常见问题)
- [更新日志](#更新日志)

---

## 项目简介

**接口自动化中台**（Inter Auto Test API）是一个面向接口测试团队的一站式管理平台，以**项目隔离**为核心模型，解决多团队、多项目场景下接口测试的管理痛点。

### 适用场景

- **多项目接口测试团队** — 每个项目独立管理接口和用例，成员权限互不干扰
- **自动化测试平台** — 通过 Open API 对接 Agent，实现接口和用例的自动注册
- **质量保障部门** — 建立接口资产库，追踪接口变更历史，数据统计分析
- **AI 辅助测试** — 集成 Dify AI 工作流，支持智能测试生成

### 核心能力矩阵

| 能力 | 说明 |
|------|------|
| 项目隔离 | 多项目独立管理，成员仅可访问被授权的项目 |
| 接口管理 | 统一维护接口名称、HTTP 方法、三环境 URL、服务编码 |
| 用例管理 | 每个接口可绑定多个测试用例，支持请求数据、断言规则、排序 |
| 在线测试 | 在 Web 界面直接执行用例测试，实时查看响应结果 |
| 数据统计 | ECharts 可视化看板：接口总数、用例分布、通过率、热力图 |
| 用户权限 | RBAC 模型（用户 → 角色 → 权限），细粒度控制管理操作 |
| Open API | Token 认证 + 批量导入接口和用例，供外部 Agent 调用 |
| AI 工作流 | 集成 Dify 工作流引擎，支持 AI 生成测试用例和智能分析 |
| 在线文档 | Knife4j 自动生成 API 文档，实时展示所有接口定义 |
| 项目文档 | README 在线预览，修改项目目录下的 README.md 即时生效 |

---

## 技术架构

### 分层架构图

```
┌──────────────────────────────────────────────────────────────────────────┐
│                            前端展示层                                     │
│  Thymeleaf + Layui 2.9 + Font Awesome 6 + ECharts 5 + marked.js         │
│  Animate.css (动画) + 项目切换器(Sidebar) + 自适应暗色/亮色主题          │
└─────────────────────────────┬────────────────────────────────────────────┘
                              │  HTTP / JSON / Thymeleaf 模板渲染
┌─────────────────────────────▼────────────────────────────────────────────┐
│                           控制层 (Controller)                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────────────┐   │
│  │ PageController│  │ AuthController│  │  DashboardController        │   │
│  │ 页面路由/README│  │ 登录/Token   │  │  数据统计(4卡片+3图表+热力图)│   │
│  ├──────────────┤  ├──────────────┤  ├──────────────────────────────┤   │
│  │ ProjectCtrl  │  │ SysUserCtrl  │  │  SysRoleCtrl / PermCtrl      │   │
│  │ 项目CRUD/成员 │  │ 用户管理     │  │  角色管理 / 权限管理         │   │
│  ├──────────────┤  ├──────────────┤  ├──────────────────────────────┤   │
│  │ ApiInterface │  │ ApiTestcase  │  │  AssignCtrl / OnlineTestCtrl │   │
│  │ 接口管理     │  │ 用例管理     │  │  分配管理 / 在线测试         │   │
│  ├──────────────┤  ├──────────────┤  ├──────────────────────────────┤   │
│  │ OpenApiCtrl  │  │ DifyCtrl     │  │                              │   │
│  │ 批量导入     │  │ AI工作流     │  │                              │   │
│  └──────────────┘  └──────────────┘  └──────────────────────────────┘   │
└─────────────────────────────┬────────────────────────────────────────────┘
                              │
┌─────────────────────────────▼────────────────────────────────────────────┐
│                          服务层 (Service)                                  │
│  ProjectService / ProjectMemberService / DashboardService                │
│  ApiInterfaceService / ApiTestcaseService / OnlineTestService            │
│  DifyService / OpenApiService / SysUserService / SysRoleService          │
│  SysPermissionService / SysUserRoleService / SysRolePermissionService    │
│  StpInterfaceImpl (Sa-Token权限/角色加载, Caffeine缓存)                  │
└─────────────────────────────┬────────────────────────────────────────────┘
                              │
┌─────────────────────────────▼────────────────────────────────────────────┐
│                      横切关注点 (AOP)                                      │
│  ProjectPermissionAspect — 项目成员权限切面（拦截所有 project API）       │
│  ApiLogAspect — 接口调用日志切面                                          │
└─────────────────────────────┬────────────────────────────────────────────┘
                              │
┌─────────────────────────────▼────────────────────────────────────────────┐
│                          数据访问层                                        │
│  MyBatis-Plus Mapper 接口 → MySQL 数据库 (auto_test_db)                  │
│  9 张核心表：project / project_member / sys_user / sys_role /            │
│  sys_permission / sys_user_role / sys_role_permission /                  │
│  api_interface / api_testcase                                            │
└──────────────────────────────────────────────────────────────────────────┘
```

### 核心依赖清单

| 组件 | 版本 | 许可证 | 用途 |
|------|------|--------|------|
| Spring Boot | 2.7.16 | Apache 2.0 | 应用框架（起步依赖） |
| Sa-Token | 1.42.0 | MIT | 登录认证、权限鉴权、JWT Token |
| MyBatis-Plus | 3.5.7 | Apache 2.0 | ORM 框架（自动 CRUD + 分页） |
| Knife4j | 4.3.0 | Apache 2.0 | OpenAPI 3 接口文档 |
| Hutool | 5.8.35 | MPL 2.0 | 工具类库（加密、文件、Http 客户端等） |
| EasyExcel | 3.1.3 | Apache 2.0 | Excel 导入导出 |
| Layui | 2.9.21 | MIT | 前端 UI 组件库 |
| ECharts | 5.x | Apache 2.0 | 数据可视化图表 |
| Thymeleaf | 3.0.x | Apache 2.0 | 服务端模板引擎 |
| Caffeine | — | Apache 2.0 | 本地缓存（权限/角色缓存） |
| Spring AOP | — | Apache 2.0 | 项目权限切面 + 日志切面 |
| BCrypt | — | Apache 2.0 | 密码加密（兼容旧版 MD5） |
| JsonPath | 2.9.0 | Apache 2.0 | 响应断言引擎 |
| Hibernate Validator | — | Apache 2.0 | JSR303 参数校验 |
| WebSocket | — | Apache 2.0 | 实时推送支持 |

---

## 核心功能

### 项目隔离管理

- **多项目独立**：每个项目拥有独立的接口、用例、成员体系
- **项目成员准入**：基于 `ProjectPermissionAspect` AOP 切面，自动校验当前用户是否为项目成员
- **成员角色**：OWNER（项目拥有者）/ ADMIN（管理员）/ MEMBER（普通成员）
- **自动创建**：新建项目时自动将创建者设为 OWNER
- **项目切换**：侧边栏顶部项目选择器，一键切换当前项目上下文

### 接口管理

- 维护接口的 **基本信息**（名称、描述、HTTP 方法）
- 支持 **三环境地址**（DEV / UAT / PRO），一键切换
- 接口 **服务编码** 标记，方便按服务维度筛选
- 支持 **启用/禁用** 控制
- 接口与 **项目绑定**，不同项目接口完全隔离

### 用例管理

- 每个接口可绑定 **多个测试用例**，关联关系清晰
- 用例包含：请求数据（JSON）、断言规则、期望结果
- 支持 **环境标签**（dev / uat / pro），按环境执行
- **排序号** 控制用例执行顺序
- 请求数据支持 **动态占位符**：`#now#`（当前时间）、`#faker_name#`（随机姓名）等

### 在线测试

- Web 界面直接执行用例测试，无需外部工具
- 选择用例 → 选择环境 → 点击执行 → 实时查看响应
- 支持临时覆盖请求 URL
- 完整的请求/响应展示

### 数据统计看板

- **4 张统计卡片**：接口总数、用例总数、用户总数、启用接口数（带动画数字跳动）
- **柱状图**：各接口用例数量分布
- **饼图**：用例环境分布（DEV / UAT / PRO）
- **仪表盘**：接口启用率
- **时间线**：最近接口/用例变更记录
- **热力图**：接口健康度概览
- **项目标杆**：上方显示当前统计所属项目信息

### 用户权限（RBAC）

- **三层权限体系**：用户 → 角色 → 权限
- 内置权限点：
  - `user:manage` — 用户管理
  - `role:manage` — 角色管理
  - `perm:manage` — 权限管理
  - `api:manage` — 接口管理
  - `case:manage` — 用例管理
- **Sa-Token 注解** `@SaCheckPermission` 细粒度控制
- **Caffeine 缓存**：角色/权限列表缓存 10 分钟，降低 DB 压力

### Dify AI 工作流

- 集成 Dify 工作流引擎 API
- 支持 AI 辅助测试分析、用例生成
- 可配置 Dify 服务地址和 API Key

### Open API

- **两步认证**：appId + appKey → 获取 Token → 携带 Token 调用接口
- **批量导入**：一次性注册多个接口及其测试用例
- **事务性提交**：单个接口失败不影响其他接口
- **详细反馈**：返回导入成功/失败统计及错误详情

---

## 快速启动

### 环境要求

| 环境 | 最低版本 | 推荐版本 |
|------|----------|----------|
| JDK | 11+ | 17 LTS |
| MySQL | 8.0+ | 8.4 LTS |
| Maven | 3.6+ | 3.9+ |

### 第一步：初始化数据库

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS auto_test_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 执行初始化脚本（位于项目 sql/ 目录下）
mysql -u root -p auto_test_db < sql/init_table.sql
```

### 第二步：修改配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/auto_test_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码
```

> 系统支持通过**环境变量**覆盖配置，详细说明见下方 [配置说明](#配置说明)。

### 第三步：启动项目

```bash
# 方式一：Maven 直接启动（推荐开发时使用）
cd inter-auto-test-api
mvn spring-boot:run

# 方式二：打包后启动（推荐生产部署）
mvn clean package -DskipTests
java -jar target/inter-auto-test-api-0.0.1-SNAPSHOT.jar
```

### 第四步：验证启动

启动成功后访问以下地址：

| 地址 | 说明 | 是否需要登录 |
|------|------|:----------:|
| http://localhost:8849/ | 系统主页 | ✅ 需要 |
| http://localhost:8849/login | 登录页面 | ❌ 开放 |
| http://localhost:8849/doc.html | Knife4j API 文档 | ❌ 开放 |

默认管理员账号：`admin` / `admin123`

---

## 项目结构

```
inter-auto-test-api                          # 项目根目录
├── README.md                                # 项目文档（在线预览）
├── pom.xml                                  # Maven 构建配置
├── sql/
│   └── init_table.sql                       # 数据库初始化脚本（含项目隔离架构）
└── src/
    ├── main/
    │   ├── java/com/lm/interautotestapi/
    │   │   ├── InterAutoTestApiApplication.java   # 启动类
    │   │   ├── aspect/
    │   │   │   └── ProjectPermissionAspect.java   # ★ 项目成员权限切面（核心）
    │   │   ├── common/
    │   │   │   ├── Result.java                    # 统一响应封装
    │   │   │   └── PasswordUtil.java              # 密码加密工具（BCrypt）
    │   │   ├── config/
    │   │   │   ├── ApiLogAspect.java              # AOP 接口日志切面
    │   │   │   ├── GlobalExceptionHandler.java    # 全局异常处理
    │   │   │   ├── HttpClientConfig.java          # HTTP 客户端配置
    │   │   │   ├── Knife4jConfig.java             # Knife4j 文档配置
    │   │   │   ├── MyMetaObjectHandler.java       # MyBatis-Plus 自动填充
    │   │   │   ├── MybatisPlusConfig.java         # MyBatis-Plus 分页配置
    │   │   │   ├── SaTokenConfigure.java          # Sa-Token 拦截器配置
    │   │   │   └── StpInterfaceImpl.java          # 权限/角色加载实现（Caffeine 缓存）
    │   │   ├── controller/
    │   │   │   ├── ApiInterfaceController.java    # 接口管理 CRUD
    │   │   │   ├── ApiTestcaseController.java     # 用例管理 CRUD
    │   │   │   ├── AssignController.java          # 角色/权限分配
    │   │   │   ├── AuthController.java            # 登录/Token/密码修改
    │   │   │   ├── DashboardController.java       # ★ 数据统计（新增）
    │   │   │   ├── DifyController.java            # ★ Dify AI 工作流（新增）
    │   │   │   ├── OnlineTestController.java      # ★ 在线测试（新增）
    │   │   │   ├── OpenApiController.java         # Open API 批量导入
    │   │   │   ├── PageController.java            # 页面路由/README 在线预览
    │   │   │   ├── ProjectController.java         # ★ 项目 CRUD + 成员管理（新增）
    │   │   │   ├── SysPermissionController.java   # 权限管理
    │   │   │   ├── SysRoleController.java         # 角色管理
    │   │   │   └── SysUserController.java         # 用户管理（含用户搜索）
    │   │   ├── entity/
    │   │   │   ├── ApiInterface.java              # 接口实体（关联 project_id）
    │   │   │   ├── ApiTestcase.java               # 用例实体（关联 project_id）
    │   │   │   ├── Project.java                   # ★ 项目实体（新增）
    │   │   │   ├── ProjectMember.java             # ★ 项目成员实体（新增）
    │   │   │   ├── SysPermission.java             # 权限实体
    │   │   │   ├── SysRole.java                   # 角色实体
    │   │   │   ├── SysRolePermission.java         # 角色-权限关联
    │   │   │   ├── SysUser.java                   # 用户实体
    │   │   │   └── SysUserRole.java               # 用户-角色关联
    │   │   ├── mapper/                            # MyBatis-Plus Mapper 接口
    │   │   ├── model/
    │   │   │   ├── BatchImportRequest.java        # Open API 批量导入请求体
    │   │   │   ├── BatchImportResponse.java       # Open API 批量导入响应体
    │   │   │   ├── BatchInterfaceItem.java        # 批量导入接口条目
    │   │   │   ├── BatchTestcaseItem.java         # 批量导入用例条目
    │   │   │   ├── DifyRequest.java               # Dify 工作流请求
    │   │   │   ├── DifyResponse.java              # Dify 工作流响应
    │   │   │   ├── OnlineTestRequest.java         # 在线测试请求体
    │   │   │   └── OnlineTestResponse.java        # 在线测试响应体
    │   │   └── service/                           # 服务层接口与实现
    │   │       ├── impl/
    │   │       │   ├── ProjectServiceImpl.java
    │   │       │   ├── ProjectMemberServiceImpl.java
    │   │       │   ├── ApiInterfaceServiceImpl.java
    │   │       │   ├── ApiTestcaseServiceImpl.java
    │   │       │   ├── OnlineTestServiceImpl.java
    │   │       │   ├── DifyServiceImpl.java
    │   │       │   ├── SysUserServiceImpl.java
    │   │       │   ├── SysRoleServiceImpl.java
    │   │       │   ├── SysPermissionServiceImpl.java
    │   │       │   ├── SysUserRoleServiceImpl.java
    │   │       │   └── SysRolePermissionServiceImpl.java
    │   │       ├── ProjectService.java
    │   │       ├── ProjectMemberService.java
    │   │       ├── ApiInterfaceService.java
    │   │       ├── ApiTestcaseService.java
    │   │       ├── OnlineTestService.java
    │   │       ├── DifyService.java
    │   │       ├── OpenApiService.java
    │   │       ├── SysUserService.java
    │   │       ├── SysRoleService.java
    │   │       ├── SysPermissionService.java
    │   │       ├── SysUserRoleService.java
    │   │       └── SysRolePermissionService.java
    │   └── resources/
    │       ├── application.yml                    # 应用配置（支持环境变量覆盖）
    │       ├── mapper/                            # MyBatis XML Mapper
    │       ├── static/                            # 静态资源
    │       └── templates/
    │           ├── index.html                     # ★ 主布局（侧边栏+项目切换器+搜索框）
    │           ├── login.html                     # 登录页
    │           └── pages/                         # Thymeleaf 页面碎片
    │               ├── dashboard.html             # ★ 数据统计看板（新增）
    │               ├── dify.html                  # ★ Dify AI 工作流界面（新增）
    │               ├── guide.html                 # ★ 用例格式指南页面（新增）
    │               ├── interface.html             # 接口管理
    │               ├── testcase.html              # 用例管理
    │               ├── user.html                  # 用户管理
    │               ├── role.html                  # 角色管理
    │               ├── permission.html            # 权限管理
    │               ├── open-api.html              # Open API 文档
    │               └── readme.html                # README 在线查看
    └── test/                                      # 单元测试
```

> 标注 ★ 的为 v2.0 项目隔离架构新增/重构的文件

---

## 项目隔离模型

### 设计思想

系统以**项目**为核心隔离单元，所有业务数据（接口、用例）均归属于某个项目。用户必须先成为项目成员，才能访问该项目下的数据。

```
┌───────────────────────────────────────────────────────┐
│                    接口自动化中台                        │
│                                                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐   │
│  │  🌟 项目 A    │  │  🌟 项目 B    │  │  🌟 项目 C    │   │
│  │              │  │              │  │              │   │
│  │ 成员: 张三   │  │ 成员: 李四   │  │ 成员: 王五   │   │
│  │     李四     │  │     张三     │  │     赵六     │   │
│  │              │  │              │  │              │   │
│  │ 接口: 20个   │  │ 接口: 15个   │  │ 接口: 8个    │   │
│  │ 用例: 45个   │  │ 用例: 32个   │  │ 用例: 12个   │   │
│  └─────────────┘  └─────────────┘  └─────────────┘   │
│                                                        │
│  成员 A 登录后 → 看到「项目 A」和「项目 B」              │
│  成员 C 登录后 → 只看到「项目 C」                        │
└───────────────────────────────────────────────────────┘
```

### 权限校验流程

```
请求到达 → ProjectPermissionAspect 拦截
  │
  ├─ URI 是否匹配 /api/project/{projectId}/... 模式？
  │    ├─ 否 → 放行（无需项目权限）
  │    └─ 是 → 进入校验
  │
  ├─ 用户是否已登录？
  │    ├─ 否 → 抛出 NotPermissionException
  │    └─ 是 → 进入下一步
  │
  ├─ 查询 project_member 表：userId + projectId 是否存在？
  │    ├─ 否 → 抛出「无权访问该项目」异常
  │    └─ 是 → 放行
```

### 项目 API 路由规范

所有项目相关的 API 均遵循统一路径格式：

```
/api/project/{projectId}/{resource}/...
```

示例：

| 路径 | 说明 |
|:-----|:------|
| `/api/project/{projectId}/interface/page` | 查询项目下的接口 |
| `/api/project/{projectId}/testcase/page` | 查询项目下的用例 |
| `/api/project/{projectId}/dashboard/stats` | 项目数据统计 |
| `/api/project/{projectId}/online-test/execute` | 项目在线测试 |
| `/api/project/{projectId}/members` | 项目成员管理 |

---

## 认证体系

### 用户登录（前端系统使用）

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**响应：**

```json
{
  "code": 200,
  "data": {
    "token": "f26a3e2c-8b1d-4f5e-9c7a-3d8e2f1a6b4c",
    "username": "admin",
    "nickname": "管理员"
  }
}
```

登录成功后，前端自动将 token 存入 `localStorage`，后续所有请求自动携带 `Authorization` 请求头。

### 密码加密方式

系统**自动兼容**两种加密方式：

- **新用户注册 / 修改密码**：使用 BCrypt 加密（`$2a$...`）
- **旧版 MD5 密码**：自动识别并升级为 BCrypt（用户登录时自动迁移）
- 无需手动迁移，对用户完全透明

### Open API 认证（外部 Agent 使用）

两步认证流程：

```
  ┌──────────────┐
  │  用户管理新增用户  │  ← 系统自动生成 AppID（16位随机）和 AppKey（32位随机）
  └──────┬───────┘
         ▼
  ┌──────────────┐      ┌──────────────────┐
  │  步骤①：获取Token │──→  POST /api/auth/getToken
  │  appId + appKey  │      { "appId": "APP_...", "appKey": "6F8E..." }
  └──────┬───────┘      └──────────────────┘
         ▼
  ┌──────────────┐      ┌────────────────────────┐
  │  步骤②：调用接口  │──→  POST /api/open/batch-import
  │  Authorization头  │      Authorization: {token}
  └──────────────┘      └────────────────────────┘
```

> ⚠️ **重要提示**：
> - AppKey **仅创建时展示一次**，请立即复制保存到安全位置
> - Token 有效期为 **24 小时**，过期后需重新获取
> - 如果用户被禁用，Token 将立即失效

### 默认管理员账户

| 字段 | 值 |
|:-----|:----|
| 用户名 | `admin` |
| 密码 | `admin123` |
| AppID | `ADMIN_APP` |
| AppKey（MD5） | `0192023a7bbd73250516f069df18b500` |

---

## 核心 API

### 认证接口

| 方法 | 路径 | 说明 | 鉴权 |
|:----|:-----|:-----|:----:|
| POST | /api/auth/login | 用户名密码登录 | 开放 |
| POST | /api/auth/getToken | appId + appKey 获取 Token | 开放 |
| POST | /api/auth/logout | 退出登录 | 需登录 |
| POST | /api/auth/changePassword | 修改密码（需提供旧密码） | 需登录 |
| GET | /api/auth/info | 获取当前用户信息与权限 | 需登录 |

### 项目管理

| 方法 | 路径 | 说明 | 权限 |
|:----|:-----|:-----|:----:|
| GET | /api/project/list | 当前用户可访问的项目列表 | 需登录 |
| GET | /api/project/page | 分页查询所有项目 | user:manage |
| GET | /api/project/{id} | 查询项目详情 | 需登录 |
| POST | /api/project | 新增项目（自动添加创建者为 OWNER） | user:manage |
| PUT | /api/project | 修改项目信息 | user:manage |
| DELETE | /api/project/{id} | 删除项目（级联删除接口、用例、成员） | user:manage |
| GET | /api/project/{projectId}/members | 查询项目成员列表 | 需登录 |
| POST | /api/project/{projectId}/members | 批量添加项目成员 | user:manage |
| DELETE | /api/project/{projectId}/members/{userId} | 移除项目成员 | user:manage |

### 接口管理（项目隔离）

| 方法 | 路径 | 说明 | 权限 |
|:----|:-----|:-----|:----:|
| GET | /api/project/{projectId}/interface/page | 分页查询接口 | api:manage |
| GET | /api/project/{projectId}/interface/{id} | 查询接口详情 | api:manage |
| POST | /api/project/{projectId}/interface | 新增接口 | api:manage |
| PUT | /api/project/{projectId}/interface | 修改接口 | api:manage |
| DELETE | /api/project/{projectId}/interface/{id} | 删除接口 | api:manage |

### 用例管理（项目隔离）

| 方法 | 路径 | 说明 | 权限 |
|:----|:-----|:-----|:----:|
| GET | /api/project/{projectId}/testcase/page | 分页查询用例 | case:manage |
| GET | /api/project/{projectId}/testcase/{id} | 查询用例详情 | case:manage |
| POST | /api/project/{projectId}/testcase | 新增用例 | case:manage |
| PUT | /api/project/{projectId}/testcase | 修改用例 | case:manage |
| DELETE | /api/project/{projectId}/testcase/{id} | 删除用例 | case:manage |

### 数据统计（项目隔离）

| 方法 | 路径 | 说明 | 权限 |
|:----|:-----|:-----|:----:|
| GET | /api/project/{projectId}/dashboard/stats | 获取项目统计数据 | api:manage |

### 在线测试

| 方法 | 路径 | 说明 | 权限 |
|:----|:-----|:-----|:----:|
| POST | /api/project/{projectId}/online-test/execute | 执行在线测试 | case:manage |

### Dify AI 工作流

| 方法 | 路径 | 说明 | 权限 |
|:----|:-----|:-----|:----:|
| POST | /api/dify/run | 执行 Dify AI 工作流 | api:manage |

### 用户 / 角色 / 权限管理

| 方法 | 路径 | 说明 | 权限 |
|:----|:-----|:-----|:----:|
| GET | /api/user/page | 分页查询用户 | user:manage |
| POST | /api/user | 新增用户（自动生成 appId + appKey） | user:manage |
| PUT | /api/user | 修改用户 | user:manage |
| DELETE | /api/user/{id} | 删除用户 | user:manage |
| GET | /api/user/search | 搜索用户（按用户名/昵称，支持模糊） | user:manage |
| GET | /api/role/page | 分页查询角色 | role:manage |
| POST | /api/role | 新增角色 | role:manage |
| PUT | /api/role | 修改角色 | role:manage |
| DELETE | /api/role/{id} | 删除角色 | role:manage |
| GET | /api/permission/page | 分页查询权限 | perm:manage |
| POST | /api/assign/userRole | 分配用户角色 | user:manage |
| POST | /api/assign/rolePerm | 分配角色权限 | perm:manage |

### Open API

| 方法 | 路径 | 说明 | 鉴权 |
|:----|:-----|:-----|:----:|
| POST | /api/open/batch-import | 批量导入接口和用例 | 需登录（Token） |
| GET | /api/open/health | 健康检查 | 开放 |

> 完整 API 详情请访问 Knife4j 文档：[http://localhost:8849/doc.html](http://localhost:8849/doc.html)

---

## Open API 调用指南

### 第一步：获取 Token

```bash
curl -X POST http://localhost:8849/api/auth/getToken \
  -H "Content-Type: application/json" \
  -d '{"appId":"ADMIN_APP","appKey":"0192023a7bbd73250516f069df18b500"}'
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "token": "f26a3e2c-8b1d-4f5e-9c7a-3d8e2f1a6b4c",
    "username": "admin"
  }
}
```

### 第二步：批量导入接口

将上一步获取的 token 放入请求头：

```bash
curl -X POST http://localhost:8849/api/open/batch-import \
  -H "Content-Type: application/json" \
  -H "Authorization: f26a3e2c-8b1d-4f5e-9c7a-3d8e2f1a6b4c" \
  -d '{"projectId":1,"interfaces":[{"apiName":"获取用户信息","apiInfo":"根据用户ID获取用户详细信息","method":"GET","urlDev":"http://dev-api.example.com/user/info","urlUat":"http://uat-api.example.com/user/info","urlPro":"http://api.example.com/user/info","serviceCode":"user-service","testcases":[{"caseTitle":"正常请求-用户存在","caseData":"{\"userId\":1}","checkRules":"$.code == 200","expectedResults":"{\"code\":200,\"data\":{\"id\":1}}","env":"uat"},{"caseTitle":"异常请求-用户不存在","caseData":"{\"userId\":99999}","checkRules":"$.code == 404","env":"uat"}]},{"apiName":"创建订单","apiInfo":"创建一笔新订单","method":"POST","urlDev":"http://dev-api.example.com/order/create","serviceCode":"order-service","testcases":[{"caseTitle":"正常创建订单","caseData":"{\"productId\":1001,\"quantity\":2,\"amount\":99.80}","env":"uat"}]}]}'
```

### 请求体结构说明

```json
{
  "projectId": 1,
  "interfaces": [
    {
      "apiName":      "string | 必填 | 接口名称",
      "apiInfo":      "string | 可选 | 接口描述",
      "method":       "string | 必填 | HTTP 方法",
      "urlDev":       "string | 可选 | 开发环境URL",
      "urlUat":       "string | 可选 | 测试环境URL",
      "urlPro":       "string | 可选 | 生产环境URL",
      "serviceCode":  "string | 可选 | 所属服务编码",
      "enabled":      "int    | 可选 | 1=启用(默认) 0=禁用",
      "testcases": [
        {
          "caseTitle":        "string | 必填 | 用例标题",
          "caseData":         "string | 可选 | 请求数据(JSON字符串)",
          "checkRules":       "string | 可选 | 断言规则(如 $.code == 200)",
          "expectedResults":  "string | 可选 | 期望响应结果",
          "env":              "string | 可选 | dev/uat/pro",
          "enabled":          "int    | 可选 | 启用状态",
          "sortOrder":        "int    | 可选 | 排序序号"
        }
      ]
    }
  ]
}
```

### 响应格式

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "success": true,
    "interfaceSuccess": 2,
    "interfaceFailed": 0,
    "testcaseSuccess": 3,
    "testcaseFailed": 0,
    "errors": []
  }
}
```

### 错误码对照表

| HTTP 状态码 | 业务码 | 说明 |
|:-----------:|:------:|:-----|
| 200 | 200 | 请求成功 |
| 400 | — | 参数校验失败（JSR303），如缺少必填字段 |
| 401 | 401 | 未认证 / appId不存在 / appKey校验失败 / Token过期 |
| 403 | 403 | 权限不足（缺少权限码） |
| 500 | 500 | 服务器内部错误 |

---

## 数据库设计

### 表结构概览

```
┌─────────────────┐       ┌──────────────────────┐       ┌──────────────────────┐
│    project      │       │    project_member     │       │      sys_user        │
│─────────────────│       │──────────────────────│       │──────────────────────│
│ id (PK)         │──1:N──│ project_id            │       │ id (PK)              │
│ project_name    │       │ user_id              │──N:1──│ username             │
│ project_code    │       │ role (OWNER/ADMIN/   │       │ password (BCrypt)    │
│ description     │       │       MEMBER)         │       │ nickname             │
│ owner_id        │       │ created_at           │       │ email                │
│ status          │       └──────────────────────┘       │ phone                │
│ created_at      │                                       │ app_id               │
│ updated_at      │                                       │ app_key (MD5)        │
└─────────────────┘                                       │ status               │
        │                                                 └──────────┬───────────┘
        │  ┌──────────────────────┐                                  │
        │  │    api_interface     │                                  │
        │  │──────────────────────│       ┌──────────────────────┐   │
        └──│ project_id (FK)     │       │   sys_user_role      │   │
           │ id (PK)             │       │──────────────────────│   │
           │ api_name            │       │ user_id              │───┘
           │ api_info            │       │ role_id              │───┐
           │ method              │       └──────────────────────┘   │
           │ url_dev/uat/pro     │                                   │
           │ service_code        │       ┌──────────────────────┐   │
           │ enabled             │       │      sys_role        │   │
           └────────┬────────────┘       │──────────────────────│   │
                    │                    │ id (PK)              │◄──┘
           ┌────────▼────────────┐       │ role_name            │
           │   api_testcase      │       │ role_code            │
           │─────────────────────│       └──────────┬───────────┘
           │ project_id (FK)     │                  │
           │ id (PK)             │                  │
           │ interface_id (FK)   │       ┌──────────▼───────────┐
           │ case_title          │       │ sys_role_permission  │
           │ case_data (JSON)    │       │──────────────────────│
           │ check_rules (JSON)  │       │ role_id              │
           │ expected_results    │       │ perm_id              │
           │ env                 │       └──────────┬───────────┘
           │ enabled             │                  │
           │ sort_order          │       ┌──────────▼───────────┐
           └─────────────────────┘       │   sys_permission     │
                                         │──────────────────────│
                                         │ id (PK)              │
                                         │ perm_name            │
                                         │ perm_code            │
                                         └──────────────────────┘
```

### 核心表说明

**project** — 项目表（v2.0 新增）
- `project_code`：唯一编码标识，业务维度区分
- `owner_id`：项目负责人（关联 sys_user）
- `status`：启用/禁用控制

**project_member** — 项目成员关联表（v2.0 新增）
- 联合唯一索引 `uk_project_user`（project_id + user_id）
- `role`：项目角色（OWNER / ADMIN / MEMBER）
- 用户必须在此表中存在记录才能访问该项目

**sys_user** — 用户表
- `app_id` / `app_key`：Open API 认证凭证，创建时自动生成
- `password` 使用 BCrypt 加密存储（兼容旧版 MD5，登录时自动迁移）
- `app_key` 使用 MD5 加密存储

**api_interface** — 接口定义表
- `project_id`：所属项目（v2.0 新增，用于项目隔离）
- `url_dev` / `url_uat` / `url_pro`：三环境独立地址
- `service_code`：按服务维度归类

**api_testcase** — 测试用例表
- `project_id`：所属项目（v2.0 新增）
- `interface_id`：关联接口（外键）
- `case_data`：请求数据（JSON 字符串）
- `check_rules`：断言规则（JSON 数组）
- `env`：用例适用的环境（dev / uat / pro）

---

## 配置说明

### application.yml 配置项

| 配置项 | 环境变量 | 默认值 | 说明 |
|--------|----------|--------|------|
| server.port | SERVER_PORT | 8849 | 服务端口 |
| spring.datasource.url | DB_URL | jdbc:mysql://... | 数据库连接地址 |
| spring.datasource.username | DB_USERNAME | root | 数据库用户名 |
| spring.datasource.password | DB_PASSWORD | 123 | 数据库密码 |
| spring.datasource.hikari.connection-timeout | DB_CONN_TIMEOUT | 10000 | 连接超时（ms） |
| spring.datasource.hikari.maximum-pool-size | DB_MAX_POOL | 10 | 最大连接数 |
| sa-token.jwt-secret-key | JWT_SECRET_KEY | inter-auto-test-api-secret-key | JWT 密钥 |
| logging.level.com.lm.interautotestapi | LOG_LEVEL | DEBUG | 应用日志级别 |
| logging.level.com.lm.interautotestapi.mapper | SQL_LOG_LEVEL | WARN | SQL 日志级别 |
| logging.file.name | LOG_PATH | logs | 日志文件路径 |
| dify.base-url | DIFY_BASE_URL | http://.../v1 | Dify 服务地址 |
| dify.api-key | DIFY_API_KEY | — | Dify API 密钥 |

> 所有配置均可通过**环境变量**覆盖，方便 Docker 部署和 CI/CD 环境。

### 缓存配置

系统使用 **Caffeine 本地缓存**：

- 权限列表缓存：`maximumSize=500, expireAfterWrite=10m`
- 角色列表缓存：`maximumSize=500, expireAfterWrite=10m`
- 新增/修改权限后，缓存自动失效（需等待 10 分钟过期或重启服务）

### 日志配置

- 日志文件：`logs/inter-auto-test.log`
- 滚动策略：最大 50MB/文件，保留 30 天，总上限 1GB
- API 调用日志通过 `ApiLogAspect` 自动记录

---

## 用例格式指南

> 详细指南请访问系统内的「用例格式指引」页面，以下为简要说明。

### 断言规则类型

| 类型 | 示例 | 说明 |
|:----|:-----|:------|
| json | `[{"json":"$.status"}]` | JSONPath 提取响应值对比 |
| exist | `[{"exist":"data"}]` | 检查字段是否存在 |
| not_exist | `[{"not_exist":"error"}]` | 检查字段是否不存在 |
| status_code | `[{"status_code":""}]` | 校验 HTTP 状态码 |
| sql | `[{"sql":["dev","SELECT id FROM user WHERE id=1"]}]` | 数据库断言 |

> 断言规则和期望结果必须**数量相同、位置一一对应**。

### 动态占位符

| 占位符 | 替换为 | 示例 |
|:-------|:-------|:-----|
| `#now#` | 当前时间 | `2026-06-07 14:30:00` |
| `#faker_name#` | 随机中文姓名 | `张三` |
| `#faker_ssn#` | 随机身份证号 | `310...` |
| `#faker_job#` | 随机职业 | `软件工程师` |

---

## 常见问题

### 1. 启动报错 "数据库连接失败"？

确认 MySQL 服务已启动，并检查 `application.yml` 中的数据库连接配置是否正确。

```bash
# 测试数据库连接
mysql -u root -p -h 127.0.0.1 -P 3306 auto_test_db
```

### 2. 登录提示 "密码错误"？

默认管理员密码为 `admin123`。如果修改过密码后忘记，可以手动执行 SQL 重置：

```sql
-- 将密码重置为 admin123（BCrypt 加密）
UPDATE sys_user SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' WHERE username = 'admin';
```

### 3. 新建用户后看不到任何项目？

新用户需要被**添加到项目成员**中：
1. 使用管理员账号登录
2. 进入「项目管理」→ 选择目标项目
3. 在「项目成员」中添加该用户
4. 用户刷新页面即可看到该项目

### 4. 部署后访问页面样式错乱？

确认未使用缓存，尝试强制刷新（`Ctrl + Shift + R`）。如仍异常，检查 Layui 等静态资源是否正确加载。

### 5. 接口/用例导入失败？

检查 JSON 格式是否正确（key 必须用双引号），并确认 Token 未过期。详细错误信息会返回在响应体的 `errors` 字段中。

---

## 更新日志

### v2.1.0（当前版本）

- ✨ 新增「数据统计看板」页面，含 4 张统计卡片 + 3 种 ECharts 图表 + 热力图
- ✨ 新增「在线测试」功能，Web 界面直接执行用例
- ✨ 新增「Dify AI 工作流」集成，支持智能测试
- ✨ 新增「用例格式指南」页面
- ✨ 新增侧边栏项目切换器，全局上下文感知
- ✨ 新增全局搜索框（快捷键 Ctrl+K）
- ✨ 新增项目成员搜索功能（按用户名/昵称模糊搜索）
- 🎨 重构 UI：暗色/亮色主题、卡片动画、渐变色彩
- 🔒 密码升级 BCrypt 加密（兼容旧版 MD5）
- ⚡ Caffeine 本地缓存，提升权限查询性能

### v2.0.0（项目隔离架构）

- ✨ **核心重构：项目隔离模型**
  - 新增 `project` / `project_member` 表
  - 新增 `ProjectPermissionAspect` AOP 切面，自动校验项目成员
  - 接口/用例全部隔离到项目维度
- ✨ 新增项目 CRUD 及成员管理
- ✨ API 路径全面升级为 `/api/project/{projectId}/...` 格式
- 🎨 前端侧边栏改造：自适应高度 + 项目选择器

### v1.0.0（初始版本）

- ✨ 接口管理：增删改查 + 三环境地址
- ✨ 用例管理：增删改查 + 断言规则 + 环境标签
- ✨ RBAC 权限：用户 → 角色 → 权限三层体系
- ✨ Open API：批量导入接口和用例
- ✨ Sa-Token 登录认证 + JWT
- ✨ Knife4j 在线 API 文档
- ✨ README 在线预览
