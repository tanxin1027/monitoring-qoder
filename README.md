# Monitoring Qoder - 医院服务器监控平台

一个基于 Spring Boot 的分布式监控平台，用于监控医院内网服务器的性能指标、MySQL 数据库和 Tomcat 应用服务器。

## 项目架构

```
monitoring-qoder/
├── monitoring-server/    # 监控服务端（部署在云服务器）
└── monitoring-agent/     # 监控Agent（部署在医院内网服务器）
```

## 技术栈

### 服务端 (monitoring-server)
- **框架**: Spring Boot 3.2.0
- **ORM**: MyBatis Plus
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **安全**: Spring Security + JWT
- **JDK**: 17

### Agent端 (monitoring-agent)
- **框架**: Spring Boot 3.2.0
- **系统信息**: OSHI (Operating System and Hardware Information)
- **HTTP客户端**: WebFlux
- **JDK**: 17

## 功能特性

### 服务端功能
- 用户管理、角色管理、菜单管理
- 医院管理（多租户数据隔离）
- 服务器性能监控（CPU、内存、磁盘、负载）
- MySQL 数据库监控
- Tomcat 应用服务器监控
- 数据可视化展示
- 告警管理
- RESTful API 接口

### Agent功能
- 自动采集服务器性能指标
- MySQL 状态监控
- Tomcat 状态监控
- 定时上报数据到服务端
- 心跳检测

## 快速开始

### 环境要求
- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.8+

### 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p < monitoring-server/src/main/resources/db/schema.sql

# 插入初始数据
mysql -u root -p < monitoring-server/src/main/resources/db/data.sql
```

### 2. 配置服务端

编辑 `monitoring-server/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/monitoring_db
    username: root
    password: your_password
  
  redis:
    host: localhost
    port: 6379

jwt:
  secret: your-secret-key-change-in-production
```

### 3. 启动服务端

```bash
cd monitoring-server
mvn spring-boot:run
```

服务端将在 `http://localhost:8080` 启动。

默认登录账号：
- 用户名: admin
- 密码: admin123

### 4. 配置Agent

编辑 `monitoring-agent/src/main/resources/application.yml`：

```yaml
agent:
  server:
    url: http://your-server-domain:8080
  
  identity:
    server-ip: ${AGENT_SERVER_IP:}
    agent-key: your-agent-key
  
  mysql:
    host: localhost
    port: 3306
    username: root
    password: your_mysql_password
  
  tomcat:
    host: localhost
    port: 8080
    username: admin
    password: your_tomcat_password
```

### 5. 启动Agent

```bash
cd monitoring-agent
mvn spring-boot:run
```

## API 接口

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出

### Agent接口
- `POST /api/agent/report` - 上报监控数据
- `GET /api/agent/heartbeat` - 心跳检测

### 仪表盘接口
- `GET /api/dashboard/statistics` - 获取统计信息
- `GET /api/dashboard/servers` - 获取服务器列表
- `GET /api/dashboard/server/{id}/metrics` - 获取服务器实时指标
- `GET /api/dashboard/server/{id}/metrics/history` - 获取历史指标
- `GET /api/dashboard/server/{id}/mysql` - 获取MySQL指标
- `GET /api/dashboard/server/{id}/tomcat` - 获取Tomcat指标

## 项目结构

```
monitoring-server/
├── src/main/java/com/monitoring/server/
│   ├── config/          # 配置类
│   ├── controller/      # 控制器
│   ├── service/         # 服务层
│   ├── mapper/          # 数据访问层
│   ├── entity/          # 实体类
│   ├── dto/             # 数据传输对象
│   ├── utils/           # 工具类
│   └── security/        # 安全配置
├── src/main/resources/
│   ├── db/              # 数据库脚本
│   └── mapper/          # MyBatis映射文件

monitoring-agent/
├── src/main/java/com/monitoring/agent/
│   ├── collector/       # 指标采集器
│   ├── sender/          # 数据发送器
│   ├── config/          # 配置类
│   └── job/             # 定时任务
```

## 监控指标说明

### 服务器指标
- CPU 使用率 (%)
- 内存使用率 (%) 和使用量 (MB)
- 磁盘使用率 (%) 和使用量 (GB)
- 系统负载 (1分钟/5分钟/15分钟)
- 进程数

### MySQL指标
- 版本信息
- 连接数 / 最大连接数
- QPS (每秒查询数)
- TPS (每秒事务数)
- 运行线程数
- 慢查询数

### Tomcat指标
- 版本信息
- 线程池状态 (最大/当前/忙碌线程数)
- 连接数
- 请求数 / 错误数
- 字节接收/发送量

## 安全说明

1. **JWT Token**: 服务端使用 JWT 进行身份认证
2. **Agent认证**: Agent 使用密钥进行身份验证
3. **数据加密**: 建议使用 HTTPS 传输数据
4. **密码加密**: 用户密码使用 BCrypt 加密存储

## 部署建议

### 服务端部署
1. 使用 Nginx 反向代理
2. 配置 HTTPS
3. 使用 systemd 管理服务
4. 配置日志轮转

### Agent部署
1. 打包为可执行 JAR
2. 使用 systemd 或 Windows Service 运行
3. 配置开机自启
4. 限制 Agent 权限

## 开发计划

- [ ] 前端管理界面
- [ ] 告警通知（邮件/短信）
- [ ] 数据报表导出
- [ ] 集群监控支持
- [ ] Docker 部署支持

## 贡献

欢迎提交 Issue 和 Pull Request。

## 许可证

MIT License
