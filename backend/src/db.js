const sqlite3 = require('sqlite3').verbose();
const path = require('path');

// 创建SQLite数据库连接
const dbPath = path.join(__dirname, '..', 'library.db');
const db = new sqlite3.Database(dbPath, (err) => {
  if (err) {
    console.error('数据库连接失败:', err);
  } else {
    console.log('数据库连接成功');
  }
});

// 导出数据库连接
module.exports = db;