# Monitoring Qoder - 医院服务器监控平台

一个基于 Spring Boot 的分布式监控平台，用于监控医院内网服务器的性能指标、MySQL 数据库和 Tomcat 应用服务器。

## 项目架构

```
monitoring-qoder/
├── monitoring-server/    # 监控服务端（部署在云服务器）
├── monitoring-agent/     # 监控Agent（部署在医院内网服务器）
└── monitoring-web/       # 前端管理界面（Vue 3 + Element Plus）
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

### 前端 (monitoring-web)
- **框架**: Vue 3 + Vite
- **UI组件库**: Element Plus
- **图表库**: ECharts
- **状态管理**: Pinia
- **路由**: Vue Router

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

### 前端功能
- 登录认证（JWT Token）
- 数据大屏
  - 服务器状态统计卡片
  - 状态分布饼图
  - 资源使用率趋势图
  - 服务器实时列表
- 服务器监控
  - CPU/内存/磁盘实时指标
  - 系统负载显示
  - 历史趋势图表（支持多时间范围）
- MySQL监控
  - 连接信息展示
  - QPS/TPS指标
  - 线程状态分布
  - 连接使用率仪表盘
- Tomcat监控
  - 线程池状态
  - 请求成功率仪表盘
  - 流量统计
  - 线程池使用率饼图
- 告警管理
  - 告警列表查询
  - 告警处理
  - 告警详情
- 系统管理
  - 用户管理（增删改查、重置密码、角色分配）
  - 角色管理（权限分配）
  - 菜单管理（树形结构）
- 医院管理
  - 医院信息管理
  - 数据权限隔离

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

### 6. 启动前端

```bash
cd monitoring-web
npm install
npm run dev
```

前端将在 `http://localhost:3000` 启动，并自动代理到后端 API。

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

monitoring-web/
├── src/
│   ├── api/             # API接口
│   ├── components/      # 公共组件
│   ├── pages/           # 页面组件
│   │   ├── monitor/     # 监控页面
│   │   ├── system/      # 系统管理页面
│   │   └── hospital/    # 医院管理页面
│   ├── router/          # 路由配置
│   ├── store/           # 状态管理
│   └── utils/           # 工具函数
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

### 前端部署
1. 构建生产环境代码：`npm run build`
2. 将 `dist` 目录部署到 Nginx
3. 配置 Nginx 反向代理到后端 API
4. 配置 HTTPS

示例 Nginx 配置：
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        root /path/to/monitoring-web/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 开发计划

- [x] 后端服务端（Spring Boot）
- [x] Agent数据采集（OSHI + JMX）
- [x] 前端管理界面（Vue 3 + Element Plus）
- [x] 数据可视化（ECharts）
- [x] 用户权限管理（RBAC）
- [ ] 告警通知（邮件/短信/钉钉）
- [ ] 数据报表导出（Excel/PDF）
- [ ] 集群监控支持
- [ ] Docker 部署支持
- [ ] 移动端适配

## 贡献

欢迎提交 Issue 和 Pull Request。

## 许可证

MIT License
