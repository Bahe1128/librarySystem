const express = require('express');
const cors = require('cors');
const dotenv = require('dotenv');

// 加载环境变量
dotenv.config();

const app = express();
const port = process.env.PORT || 5000;

// 中间件
app.use(cors());
app.use(express.json());

// 路由
app.get('/', (req, res) => {
  res.json({ message: '图书管理系统后端API' });
});

// 认证路由
const authRoutes = require('./routes/authRoutes');
app.use('/api/auth', authRoutes);

// 图书路由
const bookRoutes = require('./routes/bookRoutes');
app.use('/api/books', bookRoutes);

// 借阅记录路由
const borrowRoutes = require('./routes/borrowRoutes');
app.use('/api/borrow', borrowRoutes);

// 统计路由
const statsRoutes = require('./routes/statsRoutes');
app.use('/api/stats', statsRoutes);

// 启动服务器
app.listen(port, () => {
  console.log(`服务器运行在 http://localhost:${port}`);
});