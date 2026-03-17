-- 监控平台数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS monitoring_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE monitoring_db;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    INDEX idx_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '菜单ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_code VARCHAR(50) NOT NULL COMMENT '菜单编码',
    path VARCHAR(200) COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径',
    icon VARCHAR(50) COMMENT '图标',
    menu_type TINYINT DEFAULT 1 COMMENT '菜单类型：1-菜单，2-按钮',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    INDEX idx_parent_id (parent_id),
    INDEX idx_menu_code (menu_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 医院表
CREATE TABLE IF NOT EXISTS sys_hospital (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '医院ID',
    hospital_name VARCHAR(100) NOT NULL COMMENT '医院名称',
    hospital_code VARCHAR(50) NOT NULL UNIQUE COMMENT '医院编码',
    address VARCHAR(200) COMMENT '地址',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    description TEXT COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    INDEX idx_hospital_code (hospital_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医院表';

-- 用户医院关联表（数据权限）
CREATE TABLE IF NOT EXISTS sys_user_hospital (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    hospital_id BIGINT NOT NULL COMMENT '医院ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_hospital (user_id, hospital_id),
    INDEX idx_user_id (user_id),
    INDEX idx_hospital_id (hospital_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户医院关联表';

-- 服务器表
CREATE TABLE IF NOT EXISTS mon_server (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '服务器ID',
    hospital_id BIGINT COMMENT '所属医院ID',
    server_name VARCHAR(100) NOT NULL COMMENT '服务器名称',
    server_ip VARCHAR(50) NOT NULL COMMENT '服务器IP',
    os_type VARCHAR(50) COMMENT '操作系统类型',
    os_version VARCHAR(100) COMMENT '操作系统版本',
    cpu_cores INT COMMENT 'CPU核心数',
    memory_size BIGINT COMMENT '内存大小（MB）',
    disk_size BIGINT COMMENT '磁盘大小（GB）',
    agent_version VARCHAR(50) COMMENT 'Agent版本',
    last_heartbeat DATETIME COMMENT '最后心跳时间',
    status TINYINT DEFAULT 1 COMMENT '状态：0-离线，1-在线',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    INDEX idx_hospital_id (hospital_id),
    INDEX idx_server_ip (server_ip),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务器表';

-- 服务器性能指标表
CREATE TABLE IF NOT EXISTS mon_server_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    server_id BIGINT NOT NULL COMMENT '服务器ID',
    cpu_usage DECIMAL(5,2) COMMENT 'CPU使用率（%）',
    memory_usage DECIMAL(5,2) COMMENT '内存使用率（%）',
    memory_used DECIMAL(10,2) COMMENT '已用内存（MB）',
    memory_total DECIMAL(10,2) COMMENT '总内存（MB）',
    disk_usage DECIMAL(5,2) COMMENT '磁盘使用率（%）',
    disk_used DECIMAL(10,2) COMMENT '已用磁盘（GB）',
    disk_total DECIMAL(10,2) COMMENT '总磁盘（GB）',
    load_average1 DECIMAL(5,2) COMMENT '1分钟负载',
    load_average5 DECIMAL(5,2) COMMENT '5分钟负载',
    load_average15 DECIMAL(5,2) COMMENT '15分钟负载',
    process_count INT COMMENT '进程数',
    collect_time DATETIME NOT NULL COMMENT '采集时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_server_id (server_id),
    INDEX idx_collect_time (collect_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务器性能指标表';

-- MySQL监控指标表
CREATE TABLE IF NOT EXISTS mon_mysql_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    server_id BIGINT NOT NULL COMMENT '服务器ID',
    mysql_version VARCHAR(50) COMMENT 'MySQL版本',
    port INT COMMENT '端口号',
    qps DECIMAL(10,2) COMMENT '每秒查询数',
    tps DECIMAL(10,2) COMMENT '每秒事务数',
    connections INT COMMENT '当前连接数',
    max_connections INT COMMENT '最大连接数',
    threads_running INT COMMENT '运行线程数',
    threads_connected INT COMMENT '连接线程数',
    slow_queries DECIMAL(10,2) COMMENT '慢查询数',
    questions DECIMAL(15,2) COMMENT '总查询数',
    uptime DECIMAL(15,2) COMMENT '运行时间（秒）',
    innodb_buffer_pool_usage DECIMAL(5,2) COMMENT 'InnoDB缓冲池使用率',
    query_cache_hit_rate DECIMAL(5,2) COMMENT '查询缓存命中率',
    collect_time DATETIME NOT NULL COMMENT '采集时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_server_id (server_id),
    INDEX idx_collect_time (collect_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MySQL监控指标表';

-- Tomcat监控指标表
CREATE TABLE IF NOT EXISTS mon_tomcat_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    server_id BIGINT NOT NULL COMMENT '服务器ID',
    tomcat_version VARCHAR(50) COMMENT 'Tomcat版本',
    port INT COMMENT '端口号',
    max_threads INT COMMENT '最大线程数',
    current_threads INT COMMENT '当前线程数',
    busy_threads INT COMMENT '忙碌线程数',
    connection_count INT COMMENT '连接数',
    bytes_received DECIMAL(15,2) COMMENT '接收字节数',
    bytes_sent DECIMAL(15,2) COMMENT '发送字节数',
    request_count INT COMMENT '请求数',
    error_count INT COMMENT '错误数',
    processing_time DECIMAL(15,2) COMMENT '处理时间（ms）',
    max_time DECIMAL(15,2) COMMENT '最大处理时间（ms）',
    collect_time DATETIME NOT NULL COMMENT '采集时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_server_id (server_id),
    INDEX idx_collect_time (collect_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Tomcat监控指标表';

-- 告警记录表
CREATE TABLE IF NOT EXISTS mon_alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '告警ID',
    server_id BIGINT NOT NULL COMMENT '服务器ID',
    alert_type VARCHAR(50) COMMENT '告警类型',
    alert_level TINYINT COMMENT '告警级别：1-警告，2-严重',
    alert_title VARCHAR(200) COMMENT '告警标题',
    alert_content TEXT COMMENT '告警内容',
    status TINYINT DEFAULT 0 COMMENT '状态：0-未处理，1-已处理',
    handle_time DATETIME COMMENT '处理时间',
    handle_remark VARCHAR(500) COMMENT '处理备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_server_id (server_id),
    INDEX idx_alert_type (alert_type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警记录表';
