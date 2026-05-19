const express = require('express');
const statsController = require('../controllers/statsController');
const authMiddleware = require('../middleware/authMiddleware');

const router = express.Router();

// 需要管理员权限的路由
router.get('/system', authMiddleware.authenticate, authMiddleware.authorizeAdmin, statsController.getSystemStats);
router.get('/trends', authMiddleware.authenticate, authMiddleware.authorizeAdmin, statsController.getBorrowTrends);

module.exports = router;