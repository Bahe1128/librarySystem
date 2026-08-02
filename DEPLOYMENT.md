# 图书管理系统部署指南

## 技术栈（当前）

- **前端**：Vue 3 + Vite + Element Plus（`frontend-vue/`）
- **后端**：Java Spring Boot 3.2 + MyBatis-Plus + H2 数据库（`backend-java/`）
- **认证**：JWT（Spring Security + JJWT）

> 注意：本文档早期版本描述的 React + Express + PostgreSQL 技术栈已随重构废弃，
> 项目在 commit `3ca82a7` 迁移至 Spring Boot。

## 后端部署（backend-java/）

### 1. 环境要求

- JDK 17+
- Maven 3.6+

### 2. 构建

```bash
cd backend-java
mvn clean package
```

### 3. 配置

默认使用 H2 文件数据库（`backend-java/data/library.mv.db`），首次启动自动建表并初始化示例数据，无需额外配置数据库。

生产环境建议通过环境变量覆盖以下配置（见 `src/main/resources/application.yml`）：

| 环境变量 | 默认值 | 说明 |
|---------|--------|------|
| `JWT_SECRET` | 开发用默认密钥 | JWT 签名密钥，生产必须替换为强随机值 |
| `SERVER_PORT` | `5000` | 服务端口 |

### 4. 启动

```bash
java -jar target/library-management-system-1.0.0.jar
```

### 默认账号（开发环境示例数据）

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |
| user1 | 123456 | 普通用户 |
| user2 | 123456 | 普通用户 |

## 前端部署（frontend-vue/）

### 1. 构建

```bash
cd frontend-vue
npm install
npm run build
```

构建产物位于 `dist/`，可部署到任意静态文件服务器（Nginx、Apache、Vercel、Netlify 等）。
注意：`/api` 开头的请求需反向代理到后端服务（默认 `http://localhost:5000`）。

### 2. Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/frontend-vue/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:5000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 开发环境运行

```bash
# 后端（端口 5000）
cd backend-java
mvn spring-boot:run

# 前端（端口 5173，/api 自动代理到 5000）
cd frontend-vue
npm run dev
```

## 安全注意事项

1. 生产环境务必通过环境变量 `JWT_SECRET` 设置强随机密钥，并启用 HTTPS
2. H2 控制台（`/h2-console`）仅限管理员访问，生产环境建议彻底关闭
3. 图书写接口（新增/编辑/删除）仅限管理员；公开接口仅限图书查询、登录、注册
4. 定期备份 H2 数据库文件（`backend-java/data/library.mv.db`）

## 核心功能

1. **用户管理**：注册、登录、管理员账号管理、角色权限控制
2. **图书管理**：添加、编辑、删除、分页查询与关键词搜索
3. **借阅管理**：借阅图书、归还图书、借阅记录查询
4. **数据统计**：系统统计看板、按月借阅趋势
