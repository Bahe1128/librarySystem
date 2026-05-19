const jwt = require('jsonwebtoken');
require('dotenv').config();

const authMiddleware = {
  // 验证JWT令牌
  authenticate: (req, res, next) => {
    try {
      const token = req.headers.authorization?.split(' ')[1];
      
      if (!token) {
        return res.status(401).json({ message: '未提供认证令牌' });
      }
      
      const decoded = jwt.verify(token, process.env.JWT_SECRET || 'your-secret-key');
      req.user = decoded;
      next();
    } catch (error) {
      console.error('认证失败:', error);
      res.status(401).json({ message: '认证失败' });
    }
  },
  
  // 验证管理员权限
  authorizeAdmin: (req, res, next) => {
    if (req.user.role !== 'admin') {
      return res.status(403).json({ message: '没有管理员权限' });
    }
    next();
  }
};

module.exports = authMiddleware;