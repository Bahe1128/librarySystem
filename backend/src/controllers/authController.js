const User = require('../models/user');
const jwt = require('jsonwebtoken');
require('dotenv').config();

const authController = {
  // 注册
  register: async (req, res) => {
    try {
      const { username, password, role } = req.body;
      
      // 检查用户名是否已存在
      const existingUser = await User.findByUsername(username);
      if (existingUser) {
        return res.status(400).json({ message: '用户名已存在' });
      }
      
      // 创建新用户
      const user = await User.create(username, password, role);
      
      // 生成JWT令牌
      const token = jwt.sign(
        { id: user.id, username: user.username, role: user.role },
        process.env.JWT_SECRET || 'your-secret-key',
        { expiresIn: '7d' }
      );
      
      res.status(201).json({ user, token });
    } catch (error) {
      console.error('注册失败:', error);
      res.status(500).json({ message: '注册失败' });
    }
  },
  
  // 登录
  login: async (req, res) => {
    try {
      const { username, password } = req.body;
      
      // 查找用户
      const user = await User.findByUsername(username);
      if (!user) {
        return res.status(401).json({ message: '用户名或密码错误' });
      }
      
      // 验证密码
      const isValidPassword = await User.validatePassword(user, password);
      if (!isValidPassword) {
        return res.status(401).json({ message: '用户名或密码错误' });
      }
      
      // 生成JWT令牌
      const token = jwt.sign(
        { id: user.id, username: user.username, role: user.role },
        process.env.JWT_SECRET || 'your-secret-key',
        { expiresIn: '7d' }
      );
      
      res.status(200).json({ user: { id: user.id, username: user.username, role: user.role }, token });
    } catch (error) {
      console.error('登录失败:', error);
      res.status(500).json({ message: '登录失败' });
    }
  }
};

module.exports = authController;