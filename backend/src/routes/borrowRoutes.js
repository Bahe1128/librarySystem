const express = require('express');
const borrowController = require('../controllers/borrowController');
const authMiddleware = require('../middleware/authMiddleware');

const router = express.Router();

// 需要认证的路由
router.post('/borrow', authMiddleware.authenticate, borrowController.borrowBook);
router.get('/user', authMiddleware.authenticate, borrowController.getUserRecords);
router.post('/return', authMiddleware.authenticate, borrowController.returnBook);

// 需要管理员权限的路由
router.get('/', authMiddleware.authenticate, authMiddleware.authorizeAdmin, borrowController.getAllRecords);

module.exports = router;