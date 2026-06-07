-- ============================================================
-- 接口自动化中台 - 数据库初始化脚本
-- 版本：v2.0.0（项目隔离架构）
-- 说明：
--   1. 执行前请先创建数据库 auto_test_db
--   2. 本脚本可重复执行，所有 INSERT 均使用 IGNORE 避免重复键错误
--   3. 新增字段使用 ALTER TABLE ... ADD COLUMN IF NOT EXISTS 兼容已有库
-- ============================================================

CREATE DATABASE IF NOT EXISTS auto_test_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE auto_test_db;

-- ============================================================
-- 0. 项目表（项目隔离核心）
-- ============================================================
CREATE TABLE IF NOT EXISTS project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    project_name VARCHAR(100) NOT NULL COMMENT '项目名称',
    project_code VARCHAR(50) NOT NULL COMMENT '项目编码（唯一标识）',
    description VARCHAR(500) DEFAULT '' COMMENT '项目描述',
    owner_id BIGINT COMMENT '项目负责人ID（关联sys_user）',
    status TINYINT(1) DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_project_code (project_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- ============================================================
-- 0.1 项目成员关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS project_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT '项目角色(OWNER/ADMIN/MEMBER)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_project_id (project_id),
    KEY idx_user_id (user_id),
    UNIQUE KEY uk_project_user (project_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目成员关联表';

-- ============================================================
-- 1. 接口信息表
--    url_dev/url_uat/url_pro 均有默认值 ''（可选字段）
--    不设 api_name 唯一索引（允许同名接口存在于不同服务）
--    project_id：所属项目（项目隔离）
-- ============================================================
CREATE TABLE IF NOT EXISTS api_interface (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    api_name VARCHAR(100) NOT NULL COMMENT '接口名称',
    api_info VARCHAR(500) DEFAULT '' COMMENT '接口描述',
    url_dev VARCHAR(500) DEFAULT '' COMMENT '开发环境URL',
    url_uat VARCHAR(500) DEFAULT '' COMMENT '测试环境URL',
    url_pro VARCHAR(500) DEFAULT '' COMMENT '生产环境URL',
    method VARCHAR(10) NOT NULL COMMENT '请求方法(GET/POST/PUT/DELETE)',
    service_code VARCHAR(50) DEFAULT '' COMMENT '服务编码',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口信息表';

-- ============================================================
-- 2. 接口测试用例表
--    case_data / check_rules / expected_results 均为可空
--    （Open API 批量导入时这些字段为可选）
--    project_id：所属项目（冗余字段，便于按项目查询）
-- ============================================================
CREATE TABLE IF NOT EXISTS api_testcase (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    interface_id BIGINT NOT NULL COMMENT '关联接口ID',
    case_title VARCHAR(200) NOT NULL COMMENT '用例标题',
    case_data TEXT COMMENT '请求数据(JSON格式)',
    check_rules TEXT COMMENT '断言规则(JSON数组格式)',
    expected_results TEXT COMMENT '期望结果(JSON数组格式)',
    env VARCHAR(10) NOT NULL COMMENT '适用环境(dev/uat/pro)',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_project_id (project_id),
    KEY idx_interface_id (interface_id),
    KEY idx_env (env),
    KEY idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口测试用例表';

-- ============================================================
-- 3. 用户表
--    app_id / app_key：Open API 认证凭证，创建时自动生成
--    password：优先使用 BCrypt 加密，兼容旧版 MD5（首次登录自动升级）
--    app_key 存储 MD5 加密值
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(MD5加密)',
    nickname VARCHAR(50) DEFAULT '' COMMENT '昵称',
    email VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
    app_id VARCHAR(50) UNIQUE COMMENT '应用ID',
    app_key VARCHAR(255) COMMENT '应用密钥(MD5加密)',
    status TINYINT(1) DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 4. 角色表
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    role_desc VARCHAR(200) DEFAULT '' COMMENT '角色描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ============================================================
-- 5. 用户角色关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ============================================================
-- 6. 权限表
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    perm_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    perm_code VARCHAR(100) NOT NULL COMMENT '权限编码',
    perm_url VARCHAR(200) DEFAULT '' COMMENT '权限URL',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_perm_code (perm_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ============================================================
-- 7. 角色权限关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    perm_id BIGINT NOT NULL COMMENT '权限ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_role_id (role_id),
    KEY idx_perm_id (perm_id),
    UNIQUE KEY uk_role_perm (role_id, perm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ============================================================
-- 8. 兼容迁移：为已存在的旧表添加 project_id 等新字段
--     适用于改造前已执行过旧版 SQL 的数据库
-- ============================================================

-- 检查并添加 project_id 到 api_interface（旧版可能没有该列）
SET @dbname = (SELECT DATABASE());
SET @exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'api_interface' AND COLUMN_NAME = 'project_id');
SET @sql = IF(@exists = 0, 'ALTER TABLE api_interface ADD COLUMN project_id BIGINT NOT NULL COMMENT \'所属项目ID\' AFTER id, ADD INDEX idx_project_id (project_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 project_id 到 api_testcase（旧版可能没有该列）
SET @exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'api_testcase' AND COLUMN_NAME = 'project_id');
SET @sql = IF(@exists = 0, 'ALTER TABLE api_testcase ADD COLUMN project_id BIGINT NOT NULL COMMENT \'所属项目ID\' AFTER id, ADD INDEX idx_project_id (project_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 将旧数据的 project_id 归入默认项目（迁移专用，重复执行无害）
SET @orphanCount = (SELECT COUNT(*) FROM api_interface WHERE project_id = 0 OR project_id IS NULL);
SET @defaultPid = (SELECT id FROM project WHERE project_code = 'DEFAULT' LIMIT 1);
SET @updateIface = IF(@orphanCount > 0 AND @defaultPid IS NOT NULL, CONCAT('UPDATE api_interface SET project_id = ', @defaultPid, ' WHERE project_id = 0 OR project_id IS NULL'), 'SELECT 1');
PREPARE stmt FROM @updateIface;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @updateTc = IF(@orphanCount > 0 AND @defaultPid IS NOT NULL, CONCAT('UPDATE api_testcase SET project_id = ', @defaultPid, ' WHERE project_id = 0 OR project_id IS NULL'), 'SELECT 1');
PREPARE stmt FROM @updateTc;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 9. 初始化默认数据
--    注意插入顺序：
--      角色 → 权限 → 用户（先建用户）→ 角色权限分配 → 用户角色分配
--    所有 INSERT 使用 IGNORE，重复执行不会报错
-- ============================================================

-- 8.1 初始化角色
INSERT IGNORE INTO sys_role (role_name, role_code, role_desc) VALUES ('超级管理员', 'admin', '系统超级管理员');
INSERT IGNORE INTO sys_role (role_name, role_code, role_desc) VALUES ('普通用户', 'user', '普通用户');

-- 8.2 初始化权限（5 个核心管理权限）
INSERT IGNORE INTO sys_permission (perm_name, perm_code, perm_url, parent_id) VALUES ('接口管理', 'api:manage', '/api/interface/**', 0);
INSERT IGNORE INTO sys_permission (perm_name, perm_code, perm_url, parent_id) VALUES ('用例管理', 'case:manage', '/api/testcase/**', 0);
INSERT IGNORE INTO sys_permission (perm_name, perm_code, perm_url, parent_id) VALUES ('用户管理', 'user:manage', '/api/user/**', 0);
INSERT IGNORE INTO sys_permission (perm_name, perm_code, perm_url, parent_id) VALUES ('角色管理', 'role:manage', '/api/role/**', 0);
INSERT IGNORE INTO sys_permission (perm_name, perm_code, perm_url, parent_id) VALUES ('权限管理', 'perm:manage', '/api/permission/**', 0);

-- 8.3 初始化管理员用户
--     密码 admin123 的 MD5 = 0192023a7bbd73250516f069df18b500
--     AppKey 也是 MD5(admin123) = 0192023a7bbd73250516f069df18b500
INSERT IGNORE INTO sys_user (username, password, nickname, email, app_id, app_key, status)
VALUES ('admin', '0192023a7bbd73250516f069df18b500', '超级管理员', 'admin@example.com', 'ADMIN_APP', '0192023a7bbd73250516f069df18b500', 1);

-- 8.4 为 admin 角色分配全部 5 个权限
INSERT IGNORE INTO sys_role_permission (role_id, perm_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p WHERE r.role_code = 'admin';

-- 8.5 将 admin 用户关联到 admin 角色
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.username = 'admin' AND r.role_code = 'admin';

-- 8.6 初始化默认项目
INSERT IGNORE INTO project (project_name, project_code, description, owner_id, status)
SELECT '默认项目', 'DEFAULT', '系统默认项目，所有未分配项目的数据归属此处', id, 1 FROM sys_user WHERE username = 'admin';

-- 8.7 将 admin 用户添加为默认项目的 OWNER
INSERT IGNORE INTO project_member (project_id, user_id, role)
SELECT p.id, u.id, 'OWNER' FROM project p, sys_user u WHERE p.project_code = 'DEFAULT' AND u.username = 'admin';
