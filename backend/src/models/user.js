const db = require('../db');
const bcrypt = require('bcryptjs');

class User {
  // 创建用户
  static create(username, password, role = 'user') {
    return new Promise((resolve, reject) => {
      bcrypt.hash(password, 10, (err, hashedPassword) => {
        if (err) reject(err);
        const query = `
          INSERT INTO users (username, password, role)
          VALUES (?, ?, ?)
        `;
        db.run(query, [username, hashedPassword, role], function(err) {
          if (err) reject(err);
          // 返回创建的用户
          db.get(`SELECT id, username, role, created_at FROM users WHERE id = ?`, [this.lastID], (err, row) => {
            if (err) reject(err);
            resolve(row);
          });
        });
      });
    });
  }

  // 根据用户名查找用户
  static findByUsername(username) {
    return new Promise((resolve, reject) => {
      const query = `
        SELECT * FROM users WHERE username = ?
      `;
      db.get(query, [username], (err, row) => {
        if (err) reject(err);
        resolve(row);
      });
    });
  }

  // 根据ID查找用户
  static findById(id) {
    return new Promise((resolve, reject) => {
      const query = `
        SELECT id, username, role, created_at FROM users WHERE id = ?
      `;
      db.get(query, [id], (err, row) => {
        if (err) reject(err);
        resolve(row);
      });
    });
  }

  // 验证密码
  static async validatePassword(user, password) {
    return await bcrypt.compare(password, user.password);
  }
}

module.exports = User;