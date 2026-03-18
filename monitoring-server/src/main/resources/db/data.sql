-- 初始化数据

USE monitoring_db;

-- 插入默认角色
INSERT INTO sys_role (role_name, role_code, description, status) VALUES
('超级管理员', 'ROLE_ADMIN', '系统超级管理员，拥有所有权限', 1),
('医院管理员', 'ROLE_HOSPITAL_ADMIN', '医院管理员，管理单个医院的数据', 1),
('普通用户', 'ROLE_USER', '普通用户，只能查看数据', 1);

-- 插入默认菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, menu_type, sort, status) VALUES
-- 系统管理
(0, '系统管理', 'system', '/system', NULL, 'SettingOutlined', 1, 1, 1),
(1, '用户管理', 'system:user', '/system/user', 'system/user/index', 'UserOutlined', 1, 1, 1),
(1, '角色管理', 'system:role', '/system/role', 'system/role/index', 'TeamOutlined', 1, 2, 1),
(1, '菜单管理', 'system:menu', '/system/menu', 'system/menu/index', 'MenuOutlined', 1, 3, 1),

-- 医院管理
(0, '医院管理', 'hospital', '/hospital', NULL, 'HospitalOutlined', 1, 2, 1),
(5, '医院列表', 'hospital:list', '/hospital/list', 'hospital/list/index', 'UnorderedListOutlined', 1, 1, 1),

-- 监控管理
(0, '监控管理', 'monitor', '/monitor', NULL, 'DashboardOutlined', 1, 3, 1),
(7, '服务器监控', 'monitor:server', '/monitor/server', 'monitor/server/index', 'DesktopOutlined', 1, 1, 1),
(7, 'MySQL监控', 'monitor:mysql', '/monitor/mysql', 'monitor/mysql/index', 'DatabaseOutlined', 1, 2, 1),
(7, 'Tomcat监控', 'monitor:tomcat', '/monitor/tomcat', 'monitor/tomcat/index', 'CloudServerOutlined', 1, 3, 1),
(7, '告警管理', 'monitor:alert', '/monitor/alert', 'monitor/alert/index', 'BellOutlined', 1, 4, 1),

-- 数据可视化
(0, '数据大屏', 'dashboard', '/dashboard', 'dashboard/index', 'BarChartOutlined', 1, 0, 1);

-- 插入角色菜单关联（超级管理员拥有所有权限）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE deleted = 0;

-- 插入默认用户（密码：admin123）
-- BCrypt加密后的密码
INSERT INTO sys_user (username, password, real_name, email, phone, status) VALUES
('admin', '$2a$10$7JB720yubVSQv46y7w1quO8qEmP6r1X5qZ8Q5Z8Q5Z8Q5Z8Q5Z8Q5Z', '系统管理员', 'admin@monitoring.com', '13800138000', 1);

-- 关联超级管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 插入示例医院
INSERT INTO sys_hospital (hospital_name, hospital_code, address, contact_person, contact_phone, description, status) VALUES
('示例医院', 'DEMO001', '北京市朝阳区示例路1号', '张医生', '13800138001', '示例医院，用于测试', 1);

-- 关联用户和医院权限
INSERT INTO sys_user_hospital (user_id, hospital_id) VALUES (1, 1);

-- 插入示例服务器
INSERT INTO mon_server (hospital_id, server_name, server_ip, os_type, os_version, cpu_cores, memory_size, disk_size, agent_version, status) VALUES
(1, '示例服务器-01', '192.168.1.100', 'Linux', 'CentOS 7.9', 8, 16384, 500, '1.0.0', 1);
