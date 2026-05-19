const BorrowRecord = require('../models/borrowRecord');

const borrowController = {
  // 借阅图书
  borrowBook: async (req, res) => {
    try {
      const { bookId } = req.body;
      const userId = req.user.id;
      
      const record = await BorrowRecord.create(userId, bookId);
      res.status(201).json(record);
    } catch (error) {
      console.error('借阅图书失败:', error);
      res.status(400).json({ message: error.message });
    }
  },
  
  // 获取用户的借阅记录
  getUserRecords: async (req, res) => {
    try {
      const userId = req.user.id;
      const records = await BorrowRecord.findByUserId(userId);
      res.status(200).json(records);
    } catch (error) {
      console.error('获取借阅记录失败:', error);
      res.status(500).json({ message: '获取借阅记录失败' });
    }
  },
  
  // 获取所有借阅记录（管理员）
  getAllRecords: async (req, res) => {
    try {
      const records = await BorrowRecord.findAll();
      res.status(200).json(records);
    } catch (error) {
      console.error('获取所有借阅记录失败:', error);
      res.status(500).json({ message: '获取所有借阅记录失败' });
    }
  },
  
  // 归还图书
  returnBook: async (req, res) => {
    try {
      const { recordId } = req.body;
      const result = await BorrowRecord.returnBook(recordId);
      res.status(200).json(result);
    } catch (error) {
      console.error('归还图书失败:', error);
      res.status(400).json({ message: error.message });
    }
  }
};

module.exports = borrowController;