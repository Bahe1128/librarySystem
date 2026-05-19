const db = require('../db');

class Book {
  // 创建图书
  static create(title, author, isbn, category, totalCopies = 1) {
    return new Promise((resolve, reject) => {
      const query = `
        INSERT INTO books (title, author, isbn, category, total_copies, available_copies)
        VALUES (?, ?, ?, ?, ?, ?)
      `;
      const values = [title, author, isbn, category, totalCopies, totalCopies];
      db.run(query, values, function(err) {
        if (err) reject(err);
        // 返回创建的图书
        db.get(`SELECT * FROM books WHERE id = ?`, [this.lastID], (err, row) => {
          if (err) reject(err);
          resolve(row);
        });
      });
    });
  }

  // 获取所有图书
  static findAll() {
    return new Promise((resolve, reject) => {
      const query = `
        SELECT * FROM books
      `;
      db.all(query, (err, rows) => {
        if (err) reject(err);
        resolve(rows);
      });
    });
  }

  // 根据ID查找图书
  static findById(id) {
    return new Promise((resolve, reject) => {
      const query = `
        SELECT * FROM books WHERE id = ?
      `;
      db.get(query, [id], (err, row) => {
        if (err) reject(err);
        resolve(row);
      });
    });
  }

  // 根据关键字搜索图书
  static search(keyword) {
    return new Promise((resolve, reject) => {
      const query = `
        SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ? OR category LIKE ?
      `;
      const values = [`%${keyword}%`, `%${keyword}%`, `%${keyword}%`, `%${keyword}%`];
      db.all(query, values, (err, rows) => {
        if (err) reject(err);
        resolve(rows);
      });
    });
  }

  // 更新图书
  static update(id, title, author, isbn, category, totalCopies) {
    return new Promise((resolve, reject) => {
      // 先获取当前图书信息
      db.get(`SELECT * FROM books WHERE id = ?`, [id], (err, book) => {
        if (err) reject(err);
        if (!book) {
          resolve(null);
          return;
        }
        // 计算可用副本数的差值
        const diff = totalCopies - book.total_copies;
        const newAvailableCopies = Math.max(0, book.available_copies + diff);

        const query = `
          UPDATE books
          SET title = ?, author = ?, isbn = ?, category = ?, total_copies = ?, available_copies = ?
          WHERE id = ?
        `;
        const values = [title, author, isbn, category, totalCopies, newAvailableCopies, id];
        db.run(query, values, function(err) {
          if (err) reject(err);
          // 返回更新后的图书
          db.get(`SELECT * FROM books WHERE id = ?`, [id], (err, row) => {
            if (err) reject(err);
            resolve(row);
          });
        });
      });
    });
  }

  // 删除图书
  static delete(id) {
    return new Promise((resolve, reject) => {
      const query = `
        DELETE FROM books WHERE id = ?
      `;
      db.run(query, [id], function(err) {
        if (err) reject(err);
        resolve({ id });
      });
    });
  }

  // 更新可用副本数
  static updateAvailableCopies(id, availableCopies) {
    return new Promise((resolve, reject) => {
      const query = `
        UPDATE books
        SET available_copies = ?
        WHERE id = ?
      `;
      const values = [availableCopies, id];
      db.run(query, values, function(err) {
        if (err) reject(err);
        // 返回更新后的图书
        db.get(`SELECT * FROM books WHERE id = ?`, [id], (err, row) => {
          if (err) reject(err);
          resolve(row);
        });
      });
    });
  }
}

module.exports = Book;