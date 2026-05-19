const db = require('../db');
const Book = require('./book');

class BorrowRecord {
  // 创建借阅记录
  static async create(userId, bookId) {
    // 检查图书是否存在且有可用副本
    const book = await Book.findById(bookId);
    if (!book || book.available_copies <= 0) {
      throw new Error('图书不存在或无可用副本');
    }

    return new Promise((resolve, reject) => {
      // 开始事务
      db.run('BEGIN TRANSACTION', (err) => {
        if (err) {
          reject(err);
          return;
        }

        // 创建借阅记录
        const createRecordQuery = `
          INSERT INTO borrow_records (user_id, book_id)
          VALUES (?, ?)
        `;
        db.run(createRecordQuery, [userId, bookId], function(err) {
          if (err) {
            db.run('ROLLBACK', () => reject(err));
            return;
          }

          const recordId = this.lastID;

          // 更新图书可用副本数
          const newAvailableCopies = book.available_copies - 1;
          const updateBookQuery = `
            UPDATE books
            SET available_copies = ?
            WHERE id = ?
          `;
          db.run(updateBookQuery, [newAvailableCopies, bookId], function(err) {
            if (err) {
              db.run('ROLLBACK', () => reject(err));
              return;
            }

            // 提交事务
            db.run('COMMIT', (err) => {
              if (err) {
                reject(err);
                return;
              }

              // 返回创建的记录
              db.get(`SELECT * FROM borrow_records WHERE id = ?`, [recordId], (err, row) => {
                if (err) {
                  reject(err);
                } else {
                  resolve(row);
                }
              });
            });
          });
        });
      });
    });
  }

  // 获取用户的借阅记录
  static findByUserId(userId) {
    return new Promise((resolve, reject) => {
      const query = `
        SELECT br.*, b.title, b.author
        FROM borrow_records br
        JOIN books b ON br.book_id = b.id
        WHERE br.user_id = ?
        ORDER BY br.borrow_date DESC
      `;
      db.all(query, [userId], (err, rows) => {
        if (err) reject(err);
        resolve(rows);
      });
    });
  }

  // 获取所有借阅记录
  static findAll() {
    return new Promise((resolve, reject) => {
      const query = `
        SELECT br.*, b.title, b.author, u.username
        FROM borrow_records br
        JOIN books b ON br.book_id = b.id
        JOIN users u ON br.user_id = u.id
        ORDER BY br.borrow_date DESC
      `;
      db.all(query, (err, rows) => {
        if (err) reject(err);
        resolve(rows);
      });
    });
  }

  // 归还图书
  static returnBook(recordId) {
    return new Promise((resolve, reject) => {
      // 开始事务
      db.run('BEGIN TRANSACTION', (err) => {
        if (err) {
          reject(err);
          return;
        }

        // 查找借阅记录
        db.get(`SELECT * FROM borrow_records WHERE id = ?`, [recordId], (err, record) => {
          if (err) {
            db.run('ROLLBACK', () => reject(err));
            return;
          }

          if (!record || record.status !== 'borrowed') {
            db.run('ROLLBACK', () => reject(new Error('借阅记录不存在或已归还')));
            return;
          }

          // 更新借阅记录
          const updateRecordQuery = `
            UPDATE borrow_records
            SET return_date = CURRENT_TIMESTAMP, status = 'returned'
            WHERE id = ?
          `;
          db.run(updateRecordQuery, [recordId], function(err) {
            if (err) {
              db.run('ROLLBACK', () => reject(err));
              return;
            }

            // 更新图书可用副本数
            db.get(`SELECT * FROM books WHERE id = ?`, [record.book_id], (err, book) => {
              if (err) {
                db.run('ROLLBACK', () => reject(err));
                return;
              }

              const newAvailableCopies = book.available_copies + 1;
              const updateBookQuery = `
                UPDATE books
                SET available_copies = ?
                WHERE id = ?
              `;
              db.run(updateBookQuery, [newAvailableCopies, record.book_id], function(err) {
                if (err) {
                  db.run('ROLLBACK', () => reject(err));
                  return;
                }

                // 提交事务
                db.run('COMMIT', (err) => {
                  if (err) {
                    reject(err);
                  } else {
                    resolve({ message: '图书归还成功' });
                  }
                });
              });
            });
          });
        });
      });
    });
  }
}

module.exports = BorrowRecord;