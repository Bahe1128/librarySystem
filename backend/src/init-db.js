const db = require('./db');
const bcrypt = require('bcryptjs');

// 创建用户表
const createUsersTable = () => {
  const query = `
    CREATE TABLE IF NOT EXISTS users (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      username TEXT UNIQUE NOT NULL,
      password TEXT NOT NULL,
      role TEXT DEFAULT 'user',
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
  `;
  db.run(query, (err) => {
    if (err) {
      console.error('用户表创建失败:', err);
    } else {
      console.log('用户表创建成功');
    }
  });
};

// 创建图书表
const createBooksTable = () => {
  const query = `
    CREATE TABLE IF NOT EXISTS books (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      title TEXT NOT NULL,
      author TEXT NOT NULL,
      isbn TEXT UNIQUE,
      category TEXT,
      total_copies INTEGER DEFAULT 1,
      available_copies INTEGER DEFAULT 1,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
  `;
  db.run(query, (err) => {
    if (err) {
      console.error('图书表创建失败:', err);
    } else {
      console.log('图书表创建成功');
    }
  });
};

// 创建借阅记录表
const createBorrowRecordsTable = () => {
  const query = `
    CREATE TABLE IF NOT EXISTS borrow_records (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id INTEGER,
      book_id INTEGER,
      borrow_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      return_date TIMESTAMP,
      status TEXT DEFAULT 'borrowed',
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (user_id) REFERENCES users(id),
      FOREIGN KEY (book_id) REFERENCES books(id)
    );
  `;
  db.run(query, (err) => {
    if (err) {
      console.error('借阅记录表创建失败:', err);
    } else {
      console.log('借阅记录表创建成功');
    }
  });
};

// 创建默认管理员
const createDefaultAdmin = () => {
  db.get(`SELECT * FROM users WHERE username = 'admin'`, (err, row) => {
    if (err) {
      console.error('检查管理员失败:', err);
      return;
    }
    if (row) {
      console.log('管理员用户已存在');
      return;
    }
    const password = bcrypt.hashSync('admin123', 10);
    db.run(`INSERT INTO users (username, password, role) VALUES (?, ?, ?)`,
      ['admin', password, 'admin'],
      (err) => {
        if (err) {
          console.error('创建管理员失败:', err);
        } else {
          console.log('默认管理员创建成功 (用户名: admin, 密码: admin123)');
        }
      }
    );
  });
};

// 创建示例图书
const createSampleBooks = () => {
  db.get(`SELECT COUNT(*) AS count FROM books`, (err, row) => {
    if (err) return;
    if (row && row.count > 0) {
      console.log('图书数据已存在');
      return;
    }
    const sampleBooks = [
      ['JavaScript高级程序设计', 'Nicholas C. Zakas', '9787115275790', '计算机', 5, 5],
      ['React实战', 'Alex Banks', '9787115489549', '计算机', 3, 3],
      ['深入理解计算机系统', 'Randal E. Bryant', '9787111447447', '计算机', 4, 4],
      ['算法导论', 'Thomas H. Cormen', '9787111407010', '计算机', 3, 3],
      ['三体', '刘慈欣', '9787536692930', '科幻', 6, 6]
    ];
    const insertQuery = `INSERT INTO books (title, author, isbn, category, total_copies, available_copies) VALUES (?, ?, ?, ?, ?, ?)`;
    let completed = 0;
    sampleBooks.forEach((book) => {
      db.run(insertQuery, book, (err) => {
        if (err) {
          console.error('创建示例图书失败:', err);
        }
        completed++;
        if (completed === sampleBooks.length) {
          console.log(`已添加 ${sampleBooks.length} 本示例图书`);
        }
      });
    });
  });
};

// 初始化数据库
const initDatabase = () => {
  try {
    createUsersTable();
    createBooksTable();
    createBorrowRecordsTable();
    console.log('数据库初始化完成');
    // 延迟执行，等待表创建完成
    setTimeout(() => {
      createDefaultAdmin();
      createSampleBooks();
    }, 200);
  } catch (error) {
    console.error('数据库初始化失败:', error);
  }
};

initDatabase();
