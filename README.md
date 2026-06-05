# 接口自动化中台

> **作者：lijiangming**
>
> 一站式接口自动化测试管理平台，提供接口管理、用例管理、用户权限控制和对外 Open API 能力。
> 基于 Spring Boot + Sa-Token + MyBatis-Plus 构建，前端采用 Thymeleaf + Layui + Font Awesome。

---

## 目录

- [项目简介](#项目简介)
- [技术架构](#技术架构)
- [核心功能](#核心功能)
- [快速启动](#快速启动)
- [项目结构](#项目结构)
- [认证体系](#认证体系)
- [核心 API](#核心-api)
- [Open API 调用指南](#open-api-调用指南)
- [数据库设计](#数据库设计)
- [配置说明](#配置说明)
- [常见问题](#常见问题)
- [更新日志](#更新日志)

---

## 项目简介

**接口自动化中台**（Inter Auto Test API）是一个面向接口测试团队的一站式管理平台，旨在解决接口测试过程中接口定义分散、用例管理混乱、环境切换繁琐等痛点。

### 适用场景

- **接口测试团队** — 统一管理所有被测接口的 DEV / UAT / PRO 三环境地址
- **自动化测试平台** — 通过 Open API 对接 Agent，实现接口和用例的自动注册
- **质量保障部门** — 建立接口资产库，追踪接口变更历史

### 核心能力矩阵

| 能力 | 说明 |
|------|------|
| 接口管理 | 统一维护接口名称、HTTP 方法、三环境 URL、服务编码 |
| 用例管理 | 每个接口可绑定多个测试用例，支持请求数据、断言规则、排序 |
| 用户权限 | RBAC 模型（用户 → 角色 → 权限），细粒度控制管理操作 |
| Open API | Token 认证 + 批量导入接口和用例，供外部 Agent 调用 |
| 在线文档 | Knife4j 自动生成 API 文档，实时展示所有接口定义 |
| 项目文档 | README 在线预览，修改项目目录下的 README.md 即时生效 |

---

## 技术架构

### 分层架构图

```
┌────────────────────────────────────────────────────────────────────┐
│                        前端展示层                                    │
│   Thymeleaf 模板引擎 + Layui 组件库 + Font Awesome 图标             │
│   marked.js (Markdown 渲染) + Knife4j (API 文档)                   │
└───────────────────────────┬────────────────────────────────────────┘
                            │  HTTP / JSON
┌───────────────────────────▼────────────────────────────────────────┐
│                         控制层 (Controller)                          │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────────┐   │
│  │ PageController│  │ AuthController│  │  OpenApiController    │   │
│  │ 页面路由/README│  │ 登录/Token   │  │  批量导入(对外)       │   │
│  ├──────────────┤  ├──────────────┤  ├────────────────────────┤   │
│  │ SysUserCtrl  │  │ SysRoleCtrl  │  │  SysPermissionCtrl    │   │
│  │ 用户 CRUD    │  │ 角色 CRUD    │  │  权限 CRUD            │   │
│  ├──────────────┤  ├──────────────┤  ├────────────────────────┤   │
│  │ ApiInterface │  │ ApiTestcase  │  │  AssignController     │   │
│  │ 接口 CRUD    │  │ 用例 CRUD    │  │  用户角色/角色权限分配 │   │
│  └──────────────┘  └──────────────┘  └────────────────────────┘   │
└───────────────────────────┬────────────────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────────────────┐
│                         服务层 (Service)                             │
│   OpenApiService  │  SysUserServiceImpl  │  ApiInterfaceServiceImpl │
│   ApiTestcaseServiceImpl │  Sa-Token StpInterfaceImpl               │
└───────────────────────────┬────────────────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────────────────┐
│                        数据访问层                                    │
│   MyBatis-Plus Mapper 接口  →  MySQL 数据库 (auto_test_db)          │
│   7 张核心表：sys_user / sys_role / sys_permission /                │
│   sys_user_role / sys_role_permission / api_interface / api_testcase│
└────────────────────────────────────────────────────────────────────┘
```

### 核心依赖清单

| 组件 | 版本 | 许可证 | 用途 |
|------|------|--------|------|
| Spring Boot | 2.7.16 | Apache 2.0 | 应用框架（起步依赖） |
| Sa-Token | 1.40.0 | MIT | 登录认证、权限鉴权、JWT Token |
| MyBatis-Plus | 3.5.7 | Apache 2.0 | ORM 框架（自动 CRUD + 分页） |
| Knife4j | 4.3.0 | Apache 2.0 | OpenAPI 3 接口文档（基于 SpringDoc） |
| Hutool | 5.7.5 | MPL 2.0 | 工具类库（加密、文件、随机数等） |
| Layui | 2.9.21 | MIT | 前端 UI 组件库 |
| Thymeleaf | 3.0.x | Apache 2.0 | 服务端模板引擎 |
| Spring AOP | — | Apache 2.0 | 接口调用日志切面 |
| Hibernate Validator | — | Apache 2.0 | JSR303 参数校验 |

---

## 核心功能

### 接口管理

- 维护接口的 **基本信息**（名称、描述、HTTP 方法）
- 支持 **三环境地址**（DEV / UAT / PRO），一键切换
- 接口 **服务编码** 标记，方便按服务维度筛选
- 支持 **启用/禁用** 控制

### 用例管理

- 每个接口可绑定 **多个测试用例**，关联关系清晰
- 用例包含：请求数据（JSON）、断言规则、期望结果
- 支持 **环境标签**（dev / uat / pro），按环境执行
- **排序号** 控制用例执行顺序

### 用户权限

- **RBAC 模型**：用户 → 角色 → 权限，三层权限体系
- 内置权限点：
  - `user:manage` — 用户管理
  - `role:manage` — 角色管理
  - `perm:manage` — 权限管理
  - `api:manage` — 接口管理
  - `case:manage` — 用例管理
- **Sa-Token 注解** `@SaCheckPermission` 细粒度控制

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

> 如果数据库部署在远程服务器，将 `localhost` 改为对应的 IP 地址。

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
│   └── init_table.sql                       # 数据库初始化脚本
└── src/
    ├── main/
    │   ├── java/com/lm/interautotestapi/
    │   │   ├── InterAutoTestApiApplication.java   # 启动类
    │   │   ├── common/
    │   │   │   └── Result.java                    # 统一响应封装
    │   │   ├── config/
    │   │   │   ├── GlobalExceptionHandler.java    # 全局异常处理
    │   │   │   ├── Knife4jConfig.java             # Knife4j 文档配置
    │   │   │   ├── LogAspect.java                 # AOP 日志切面
    │   │   │   ├── MybatisPlusConfig.java         # MyBatis-Plus 配置
    │   │   │   ├── SaTokenConfigure.java          # Sa-Token 拦截器配置
    │   │   │   └── StpInterfaceImpl.java          # 权限加载实现
    │   │   ├── controller/
    │   │   │   ├── ApiInterfaceController.java    # 接口管理
    │   │   │   ├── ApiTestcaseController.java     # 用例管理
    │   │   │   ├── AssignController.java          # 角色/权限分配
    │   │   │   ├── AuthController.java            # 登录/Token
    │   │   │   ├── OpenApiController.java         # Open API（对外）
    │   │   │   ├── PageController.java            # 页面路由/README
    │   │   │   ├── SysPermissionController.java   # 权限管理
    │   │   │   ├── SysRoleController.java         # 角色管理
    │   │   │   └── SysUserController.java         # 用户管理
    │   │   ├── entity/                            # 实体类
    │   │   ├── mapper/                            # MyBatis Mapper
    │   │   ├── model/                             # DTO
    │   │   └── service/                           # 服务层
    │   └── resources/
    │       ├── application.yml                    # 应用配置
    │       ├── mapper/                            # XML Mapper
    │       ├── static/                            # 静态资源
    │       └── templates/
    │           ├── index.html                     # 主布局（侧边栏 + 面板）
    │           ├── login.html                     # 登录页
    │           └── pages/                         # 页面碎片
    │               ├── interface.html             # 接口管理
    │               ├── testcase.html              # 用例管理
    │               ├── user.html                  # 用户管理
    │               ├── role.html                  # 角色管理
    │               ├── permission.html            # 权限管理
    │               ├── open-api.html              # Open API 文档
    │               └── readme.html                # README 在线查看
    └── test/                                      # 测试
```

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

// 响应
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
| GET | /api/auth/info | 获取当前用户信息与权限 | 需登录 |

### 接口管理

| 方法 | 路径 | 说明 | 权限 |
|:----|:-----|:-----|:----:|
| GET | /api/interface/page | 分页查询接口 | api:manage |
| GET | /api/interface/{id} | 查询接口详情 | api:manage |
| POST | /api/interface | 新增接口 | api:manage |
| PUT | /api/interface | 修改接口 | api:manage |
| DELETE | /api/interface/{id} | 删除接口 | api:manage |

### 用例管理

| 方法 | 路径 | 说明 | 权限 |
|:----|:-----|:-----|:----:|
| GET | /api/testcase/page | 分页查询用例（按接口筛选） | case:manage |
| GET | /api/testcase/{id} | 查询用例详情 | case:manage |
| POST | /api/testcase | 新增用例 | case:manage |
| PUT | /api/testcase | 修改用例 | case:manage |
| DELETE | /api/testcase/{id} | 删除用例 | case:manage |

### 用户 / 角色 / 权限管理

| 方法 | 路径 | 说明 | 权限 |
|:----|:-----|:-----|:----:|
| GET | /api/user/page | 分页查询用户 | user:manage |
| POST | /api/user | 新增用户（自动生成 appId + appKey） | user:manage |
| PUT | /api/user | 修改用户 | user:manage |
| DELETE | /api/user/{id} | 删除用户 | user:manage |
| GET | /api/role/page | 分页查询角色 | role:manage |
| POST | /api/role | 新增角色 | role:manage |
| POST | /api/assign/userRole | 分配用户角色 | user:manage |
| POST | /api/assign/rolePerm | 分配角色权限 | perm:manage |

### Open API

| 方法 | 路径 | 说明 | 鉴权 |
|:----|:-----|:-----|:----:|
| POST | /api/open/batch-import | 批量导入接口和用例 | 需登录（Token） |
| GET | /api/open/health | 健康检查 | 开放 |

> 完整 API 详情请访问 Knife4j 文档：[http://localhost:8849/doc.html](http://localhost:8849/doc.html)
> 或查看系统侧边栏「Open API 文档」

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
    "expire": 86400
  }
}
```

### 第二步：批量导入接口

将上一步获取的 token 放入请求头：

```bash
curl -X POST http://localhost:8849/api/open/batch-import \
  -H "Content-Type: application/json" \
  -H "Authorization: f26a3e2c-8b1d-4f5e-9c7a-3d8e2f1a6b4c" \
  -d '{"interfaces":[{"apiName":"获取用户信息","apiInfo":"根据用户ID获取用户详细信息","method":"GET","urlDev":"http://dev-api.example.com/user/info","urlUat":"http://uat-api.example.com/user/info","urlPro":"http://api.example.com/user/info","serviceCode":"user-service","testcases":[{"caseTitle":"正常请求-用户存在","caseData":"{\"userId\":1}","checkRules":"$.code == 200","expectedResults":"{\"code\":200,\"data\":{\"id\":1}}","env":"uat"},{"caseTitle":"异常请求-用户不存在","caseData":"{\"userId\":99999}","checkRules":"$.code == 404","env":"uat"}]},{"apiName":"创建订单","apiInfo":"创建一笔新订单","method":"POST","urlDev":"http://dev-api.example.com/order/create","serviceCode":"order-service","testcases":[{"caseTitle":"正常创建订单","caseData":"{\"productId\":1001,\"quantity\":2,\"amount\":99.80}","env":"uat"}]}]}'
```

### 请求体结构说明

```json
{
  "interfaces": [
    {
      "apiName":      "string  | 必填 | 接口名称",
      "apiInfo":      "string  | 可选 | 接口描述",
      "method":       "string  | 必填 | HTTP 方法: GET/POST/PUT/DELETE/PATCH",
      "urlDev":       "string  | 可选 | 开发环境URL",
      "urlUat":       "string  | 可选 | 测试环境URL",
      "urlPro":       "string  | 可选 | 生产环境URL",
      "serviceCode":  "string  | 可选 | 所属服务编码",
      "enabled":      "int     | 可选 | 启用状态: 1=启用(默认), 0=禁用",
      "testcases": [
        {
          "caseTitle":        "string | 必填 | 用例标题",
          "caseData":         "string | 可选 | 请求数据(JSON字符串)",
          "checkRules":       "string | 可选 | 断言规则(如 $.code == 200)",
          "expectedResults":  "string | 可选 | 期望响应结果",
          "env":              "string | 可选 | 环境: dev/uat/pro",
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
| 403 | 403 | 权限不足（缺少 user:manage 或 api:manage 权限） |
| 500 | 500 | 服务器内部错误 |

---

## 数据库设计

### 表结构概览

```
┌─────────────────┐       ┌──────────────────────┐       ┌──────────────────────┐
│   sys_user      │       │    sys_user_role      │       │      sys_role        │
│─────────────────│       │──────────────────────│       │──────────────────────│
│ id (PK)         │──1:N──│ user_id              │──N:1──│ id (PK)              │
│ username        │       │ role_id              │       │ role_name            │
│ password (MD5)  │       └──────────────────────┘       │ role_code            │
│ nickname        │                                       └──────────┬───────────┘
│ email           │                                                  │
│ phone           │       ┌──────────────────────┐       ┌──────────┴───────────┐
│ app_id          │       │ sys_role_permission   │       │   sys_permission     │
│ app_key (MD5)   │       │──────────────────────│       │──────────────────────│
│ status          │──N:1──│ role_id              │──N:1──│ id (PK)              │
└─────────────────┘       │ permission_id        │       │ perm_name            │
                          └──────────────────────┘       │ perm_code            │
                                                         └──────────────────────┘

┌─────────────────────┐       ┌─────────────────────┐
│   api_interface     │       │   api_testcase       │
│─────────────────────│       │─────────────────────│
│ id (PK)             │──1:N──│ id (PK)             │
│ api_name            │       │ interface_id (FK)   │
│ api_info            │       │ case_title          │
│ method              │       │ case_data           │
│ url_dev             │       │ check_rules         │
│ url_uat             │       │ expected_results    │
│ url_pro             │       │ env                 │
│ service_code        │       │ enabled             │
│ enabled             │       │ sort_order          │
└─────────────────────┘       └─────────────────────┘
```

### 核心表说明

**sys_user** — 用户表
- `app_id` / `app_key`：Open API 认证凭证，创建时自动生成
- `password` 和 `app_key` 均使用 MD5 加密存储

**api_interface** — 接口定义表
- `url_dev` / `url_uat` / `url_pro`：三环境独立地址
- `service_code`：按服务维度归类

**api_testcase** — 测试用例表
- `interface_id`：关联接口（外键）
- `case_data`：请求数据（JSON 字符串）
- `check_rules`：断言规则
- `env`：用例适用的环境

---

## 配置说明

### application.yml 核心配置

```yaml
server:
  port: 8849                    # 服务端口

spring:
  datasource:
    url: jdbc:mysql://...       # 数据库连接
    username: root
    password: 你的密码
    hikari:
      connection-timeout: 10000 # 连接超时(ms)
      maximum-pool-size: 10     # 最大连接池

sa-token:
  token-name: Authorization     # Token 名称（请求头）
  timeout: 86400                # Token 有效期（秒）= 24小时
  token-style: uuid             # Token 格式
  is-concurrent: true           # 允许同一账号并发登录
  jwt-secret-key: inter-auto-test-api-secret-key  # JWT 密钥

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true  # 驼峰命名映射
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # SQL 日志
```

> ⚠️ `jwt-secret-key` 生产环境请更换为随机密钥，确保安全性。

---

## 常见问题

### Q1：启动报错「数据库连接失败」

**原因**：MySQL 未启动或连接信息配置错误。

**解决**：
1. 确认 MySQL 服务已启动：`net start mysql`
2. 检查 `application.yml` 中的数据库地址、用户名、密码
3. 确认数据库 `auto_test_db` 已创建

### Q2：登录提示「密码错误」

**原因**：初始 SQL 中的密码使用 MD5 加密。

**解决**：
- 默认密码 `admin123` 的 MD5 值为 `0192023a7bbd73250516f069df18b500`
- 如果修改过密码，确认数据库中存储的是 MD5 加密后的值

### Q3：Open API 返回 401

**原因**：
1. appId 或 appKey 错误
2. Token 已过期（有效期 24 小时）
3. 用户被禁用

**解决**：
1. 确认在用户管理中创建了用户并复制了正确的 AppID 和 AppKey
2. 重新调用 `/api/auth/getToken` 获取新 Token
3. 在用户管理中检查用户状态

### Q4：访问 doc.html 空白

**原因**：Knife4j 依赖的 CDN 资源加载失败（需要网络）。

**解决**：确认服务器可以访问 CDN 资源，或配置内网镜像。

### Q5：如何修改 README 在线预览内容？

直接编辑项目根目录下的 `README.md` 文件，保存后在系统页面点击「刷新」按钮即可看到最新内容，**无需重启服务**。

---

## 更新日志

### v1.0.0 (2026-06)

- 🎉 **初始版本发布**
- ✨ 接口管理：CRUD + 三环境地址 + 服务编码
- ✨ 用例管理：CRUD + 接口关联 + 断言规则 + 环境标签
- ✨ 用户管理：RBAC 权限模型 + appId/appKey 自动生成
- ✨ Open API：Token 认证 + 批量导入接口和用例
- ✨ 在线文档：Knife4j + README 在线预览
- ✨ 安全加固：MD5 密码加密 + Sa-Token 鉴权 + 参数校验
- 🔧 技术栈：Spring Boot 2.7 + MyBatis-Plus 3.5 + Sa-Token 1.4
