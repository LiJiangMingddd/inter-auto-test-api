-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS auto_test_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE auto_test_db;

-- 接口信息表
CREATE TABLE IF NOT EXISTS api_interface (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    api_name VARCHAR(100) NOT NULL COMMENT '接口名称',
    api_info VARCHAR(500) DEFAULT '' COMMENT '接口描述',
    url_dev VARCHAR(500) NOT NULL COMMENT '开发环境URL',
    url_uat VARCHAR(500) NOT NULL COMMENT '测试环境URL',
    url_pro VARCHAR(500) NOT NULL COMMENT '生产环境URL',
    method VARCHAR(10) NOT NULL COMMENT '请求方法(GET/POST/PUT/DELETE)',
    service_code VARCHAR(50) DEFAULT '' COMMENT '服务编码',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_api_name (api_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口信息表';

-- 接口测试用例表
CREATE TABLE IF NOT EXISTS api_testcase (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
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
    KEY idx_interface_id (interface_id),
    KEY idx_env (env),
    KEY idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口测试用例表';

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    nickname VARCHAR(50) DEFAULT '' COMMENT '昵称',
    email VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
    app_id VARCHAR(50) UNIQUE COMMENT '应用ID',
    app_key VARCHAR(255) COMMENT '应用密钥(加密)',
    status TINYINT(1) DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    role_desc VARCHAR(200) DEFAULT '' COMMENT '角色描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 权限表
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

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    perm_id BIGINT NOT NULL COMMENT '权限ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_role_id (role_id),
    KEY idx_perm_id (perm_id),
    UNIQUE KEY uk_role_perm (role_id, perm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 初始化默认数据
INSERT INTO sys_role (role_name, role_code, role_desc) VALUES ('超级管理员', 'admin', '系统超级管理员');
INSERT INTO sys_role (role_name, role_code, role_desc) VALUES ('普通用户', 'user', '普通用户');

INSERT INTO sys_permission (perm_name, perm_code, perm_url, parent_id) VALUES ('接口管理', 'api:manage', '/api/interface/**', 0);
INSERT INTO sys_permission (perm_name, perm_code, perm_url, parent_id) VALUES ('用例管理', 'case:manage', '/api/testcase/**', 0);
INSERT INTO sys_permission (perm_name, perm_code, perm_url, parent_id) VALUES ('用户管理', 'user:manage', '/api/user/**', 0);
INSERT INTO sys_permission (perm_name, perm_code, perm_url, parent_id) VALUES ('角色管理', 'role:manage', '/api/role/**', 0);
INSERT INTO sys_permission (perm_name, perm_code, perm_url, parent_id) VALUES ('权限管理', 'perm:manage', '/api/permission/**', 0);

-- 注意：必须先插入用户，再分配角色（sys_user_role 依赖于 sys_user）
INSERT INTO sys_user (username, password, nickname, email, app_id, app_key, status)
VALUES ('admin', '0192023a7bbd73250516f069df18b500', '超级管理员', 'admin@example.com', 'ADMIN_APP', '0192023a7bbd73250516f069df18b500', 1);

-- 为 admin 用户分配所有权限（通过 admin 角色）
INSERT INTO sys_role_permission (role_id, perm_id) SELECT r.id, p.id FROM sys_role r, sys_permission p WHERE r.role_code = 'admin';

-- 将 admin 用户关联到 admin 角色
INSERT INTO sys_user_role (user_id, role_id) SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.username = 'admin' AND r.role_code = 'admin';
