// API 接口封装层 - 集中管理所有后端 API 请求
// 使用 axios 作为 HTTP 客户端，统一配置请求拦截、响应拦截和错误处理

import axios from 'axios'

// 创建 axios 实例，预设公共配置
const request = axios.create({
  // 所有请求的基础路径，会与 Vite 代理配合转发到后端
  baseURL: '/api',
  // 请求超时时间（毫秒）
  timeout: 10000
})

// --- 请求拦截器：在每次请求发送前自动添加认证信息 ---
request.interceptors.request.use(config => {
  // 从本地存储中读取 JWT Token
  const token = localStorage.getItem('token')
  if (token) {
    // 将 Token 添加到请求头的 Authorization 字段（Bearer 认证方式）
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// --- 响应拦截器：统一处理响应数据和错误 ---
request.interceptors.response.use(
  // 成功响应处理
  response => {
    const data = response.data
    // 如果后端返回 code 为 401，说明 Token 已过期或无效
    if (data.code === 401) {
      // 清除本地存储的认证信息
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      // 重定向到登录页
      window.location.href = '/login'
      return Promise.reject(new Error(data.message))
    }
    // 返回响应数据
    return data
  },
  // HTTP 错误处理
  error => {
    // 如果 HTTP 状态码为 401（未授权）
    if (error.response?.status === 401) {
      // 清除本地认证信息并跳转到登录页
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// ==================== 认证相关 API ====================

export const authAPI = {
  // 用户登录
  login: (username, password) => request.post('/auth/login', { username, password }),
  // 用户注册
  register: (username, password) => request.post('/auth/register', { username, password })
}

// ==================== 图书管理 API ====================

export const bookAPI = {
  // 获取图书列表（分页）
  getAll: (page = 1, pageSize = 10) => request.get('/books', { params: { page, pageSize } }),
  // 搜索图书（分页）：根据关键词匹配书名、作者、ISBN 或分类
  search: (keyword, page = 1, pageSize = 10) => request.get('/books/search', { params: { keyword, page, pageSize } }),
  // 根据 ID 获取单本图书详情
  getById: (id) => request.get(`/books/${id}`),
  // 新增图书
  create: (data) => request.post('/books', data),
  // 更新图书信息
  update: (id, data) => request.put(`/books/${id}`, data),
  // 删除图书
  delete: (id) => request.delete(`/books/${id}`)
}

// ==================== 借阅管理 API ====================

export const borrowAPI = {
  // 借阅图书：传入图书 ID
  borrow: (bookId) => request.post('/borrow', { bookId }),
  // 获取借阅记录列表（分页），管理员可查看所有用户的记录
  getRecords: (page = 1, pageSize = 10) => request.get('/borrow', { params: { page, pageSize } }),
  // 归还图书：传入借阅记录 ID
  returnBook: (recordId) => request.post('/borrow/return', { recordId })
}

// ==================== 用户管理 API ====================

export const userAPI = {
  // 获取所有普通用户列表
  getAll: () => request.get('/users'),
  // 根据 ID 获取用户详情
  getById: (id) => request.get(`/users/${id}`),
  // 创建新用户
  create: (data) => request.post('/users', data),
  // 更新用户信息
  update: (id, data) => request.put(`/users/${id}`, data),
  // 删除用户
  delete: (id) => request.delete(`/users/${id}`)
}

// ==================== 统计数据 API ====================

export const statsAPI = {
  // 获取首页统计数据（馆藏数量、借出数量、注册用户数等）
  getStats: () => request.get('/stats'),
  // 获取借阅趋势数据
  getTrends: () => request.get('/stats/trends')
}
