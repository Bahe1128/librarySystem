const express = require('express');
const bookController = require('../controllers/bookController');
const authMiddleware = require('../middleware/authMiddleware');

const router = express.Router();

// 无需认证的路由
router.get('/', bookController.getAll);
router.get('/search', bookController.search);
router.get('/:id', bookController.getById);

// 需要认证的路由
router.post('/', authMiddleware.authenticate, authMiddleware.authorizeAdmin, bookController.create);
router.put('/:id', authMiddleware.authenticate, authMiddleware.authorizeAdmin, bookController.update);
router.delete('/:id', authMiddleware.authenticate, authMiddleware.authorizeAdmin, bookController.delete);

module.exports = router;