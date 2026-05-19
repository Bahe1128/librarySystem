# 图书管理系统部署指南

## 前端部署

### 1. 安装依赖
```bash
cd frontend
npm install
```

### 2. 构建项目
```bash
npm run build
```

### 3. 部署构建产物
将 `dist` 目录中的文件部署到静态文件服务器，如 Nginx、Apache 或 Vercel、Netlify 等云服务。

## 后端部署

### 1. 安装依赖
```bash
cd backend
npm install
```

### 2. 配置环境变量
创建 `.env` 文件，配置数据库连接信息和JWT密钥：
```
PORT=5000
DB_USER=postgres
DB_HOST=localhost
DB_NAME=library_management_system
DB_PASSWORD=password
DB_PORT=5432
JWT_SECRET=your-secret-key
```

### 3. 初始化数据库
```bash
node src/init-db.js
```

### 4. 启动服务器
```bash
npm start
```

## 开发环境运行

### 前端开发服务器
```bash
cd frontend
npm run dev
```

### 后端开发服务器
```bash
cd backend
npm run dev
```

## 系统架构

- **前端**：React + Vite
- **后端**：Express.js + PostgreSQL
- **认证**：JWT
- **API 接口**：RESTful API

## 核心功能

1. **用户管理**：注册、登录、权限控制
2. **图书管理**：添加、编辑、删除、查询图书
3. **借阅管理**：借阅图书、归还图书、查看借阅记录
4. **数据统计**：系统统计、借阅趋势

## 注意事项

1. 确保 PostgreSQL 数据库已安装并运行
2. 确保数据库连接信息正确配置
3. 生产环境中应使用强JWT密钥
4. 生产环境中应启用HTTPS
5. 定期备份数据库