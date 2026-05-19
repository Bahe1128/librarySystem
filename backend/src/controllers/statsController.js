const db = require('../db');

const statsController = {
  // 获取系统统计数据
  getSystemStats: (req, res) => {
    try {
      // 并行执行所有查询
      const queries = [
        new Promise((resolve, reject) => {
          db.get('SELECT COUNT(*) AS total FROM books', (err, row) => {
            if (err) reject(err);
            resolve(row.total);
          });
        }),
        new Promise((resolve, reject) => {
          db.get('SELECT COUNT(*) AS available FROM books WHERE available_copies > 0', (err, row) => {
            if (err) reject(err);
            resolve(row.available);
          });
        }),
        new Promise((resolve, reject) => {
          db.get('SELECT COUNT(*) AS total FROM users', (err, row) => {
            if (err) reject(err);
            resolve(row.total);
          });
        }),
        new Promise((resolve, reject) => {
          db.get('SELECT COUNT(*) AS borrowed FROM borrow_records WHERE status = \'borrowed\'', (err, row) => {
            if (err) reject(err);
            resolve(row.borrowed);
          });
        }),
        new Promise((resolve, reject) => {
          db.all('SELECT category, COUNT(*) AS count FROM books GROUP BY category', (err, rows) => {
            if (err) reject(err);
            resolve(rows);
          });
        })
      ];

      Promise.all(queries)
        .then(([totalBooks, availableBooks, totalUsers, borrowedBooks, categoryStats]) => {
          res.status(200).json({
            totalBooks,
            availableBooks,
            totalUsers,
            borrowedBooks,
            categoryStats
          });
        })
        .catch(error => {
          console.error('获取统计数据失败:', error);
          res.status(500).json({ message: '获取统计数据失败' });
        });
    } catch (error) {
      console.error('获取统计数据失败:', error);
      res.status(500).json({ message: '获取统计数据失败' });
    }
  },
  
  // 获取借阅趋势
  getBorrowTrends: (req, res) => {
    try {
      const query = `
        SELECT 
          strftime('%Y-%m', borrow_date) AS month,
          COUNT(*) AS count
        FROM borrow_records
        GROUP BY month
        ORDER BY month
      `;
      db.all(query, (err, rows) => {
        if (err) {
          console.error('获取借阅趋势失败:', err);
          res.status(500).json({ message: '获取借阅趋势失败' });
        } else {
          res.status(200).json(rows);
        }
      });
    } catch (error) {
      console.error('获取借阅趋势失败:', error);
      res.status(500).json({ message: '获取借阅趋势失败' });
    }
  }
};

module.exports = statsController;