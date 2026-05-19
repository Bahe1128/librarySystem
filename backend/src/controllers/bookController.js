const Book = require('../models/book');

const bookController = {
  // 创建图书
  create: async (req, res) => {
    try {
      const { title, author, isbn, category, totalCopies } = req.body;
      const book = await Book.create(title, author, isbn, category, totalCopies);
      res.status(201).json(book);
    } catch (error) {
      console.error('创建图书失败:', error);
      res.status(500).json({ message: '创建图书失败' });
    }
  },
  
  // 获取所有图书
  getAll: async (req, res) => {
    try {
      const books = await Book.findAll();
      res.status(200).json(books);
    } catch (error) {
      console.error('获取图书列表失败:', error);
      res.status(500).json({ message: '获取图书列表失败' });
    }
  },
  
  // 根据ID获取图书
  getById: async (req, res) => {
    try {
      const { id } = req.params;
      const book = await Book.findById(id);
      if (!book) {
        return res.status(404).json({ message: '图书不存在' });
      }
      res.status(200).json(book);
    } catch (error) {
      console.error('获取图书失败:', error);
      res.status(500).json({ message: '获取图书失败' });
    }
  },
  
  // 搜索图书
  search: async (req, res) => {
    try {
      const { keyword } = req.query;
      const books = await Book.search(keyword);
      res.status(200).json(books);
    } catch (error) {
      console.error('搜索图书失败:', error);
      res.status(500).json({ message: '搜索图书失败' });
    }
  },
  
  // 更新图书
  update: async (req, res) => {
    try {
      const { id } = req.params;
      const { title, author, isbn, category, totalCopies } = req.body;
      const book = await Book.update(id, title, author, isbn, category, totalCopies);
      if (!book) {
        return res.status(404).json({ message: '图书不存在' });
      }
      res.status(200).json(book);
    } catch (error) {
      console.error('更新图书失败:', error);
      res.status(500).json({ message: '更新图书失败' });
    }
  },
  
  // 删除图书
  delete: async (req, res) => {
    try {
      const { id } = req.params;
      const book = await Book.delete(id);
      if (!book) {
        return res.status(404).json({ message: '图书不存在' });
      }
      res.status(200).json({ message: '图书删除成功' });
    } catch (error) {
      console.error('删除图书失败:', error);
      res.status(500).json({ message: '删除图书失败' });
    }
  }
};

module.exports = bookController;