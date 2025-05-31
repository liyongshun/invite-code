# 邀请码管理系统

邀请码管理系统是一个用于生成、验证和管理邀请码的Web应用程序。该系统旨在为公司内部AI IDE工具的灰度发布提供支持，通过邀请码机制控制用户访问权限。

## 系统架构

本系统采用前后端分离的架构：

- 前端：React + Ant Design，提供用户友好的界面
- 后端：Spring Boot，提供RESTful API服务
- 数据库：MySQL，存储邀请码和使用记录数据

## 功能特性

### 用户功能
- 提交邀请码获取AI IDE使用资格
- 验证邀请码有效性
- 成功验证后获取下载链接

### 管理员功能
- 批量生成邀请码
- 查看邀请码列表
- 查看邀请码详情
- 查看邀请码使用记录
- 启用/禁用邀请码

## 开发环境要求

- JDK 11+
- Maven 3.6+
- Node.js 14+
- MySQL 5.7+

## 安装与运行

### 后端服务

1. 配置数据库
```sql
CREATE DATABASE invite_code_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改配置
编辑 `backend/src/main/resources/application.properties` 文件，设置数据库连接信息

3. 构建与运行
```bash
cd backend
mvn clean package
java -jar target/invite-code-0.0.1-SNAPSHOT.jar
```

### 前端应用

1. 安装依赖
```bash
cd frontend
npm install
```

2. 启动开发服务器
```bash
npm start
```

3. 构建生产版本
```bash
npm run build
```

## API文档

### 公开API
- `POST /api/invite-codes/verify` - 验证邀请码

### 管理员API（需要认证）
- `POST /api/invite-codes/generate` - 生成邀请码
- `GET /api/invite-codes` - 获取邀请码列表
- `GET /api/invite-codes/{id}` - 获取邀请码详情
- `GET /api/invite-codes/{id}/usage-records` - 获取邀请码使用记录
- `PUT /api/invite-codes/{id}/disable` - 禁用邀请码
- `PUT /api/invite-codes/{id}/enable` - 启用邀请码

## 安全考虑

- 所有管理员API都需要JWT认证
- 邀请码采用随机生成算法，确保不可预测性
- 系统记录每次邀请码使用的IP地址和浏览器信息

## 未来计划

- 与公司SSO系统集成，实现用户身份认证
- 增加邀请码使用统计和分析功能
- 提供邀请码导出功能 