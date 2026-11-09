// API 接口封装层 - 集中管理所有后端 API 请求
//
// 统一响应契约：
//   - 后端所有接口返回 { code, message, data }，成功时 code=200。
//   - 本拦截器对成功响应「直接解包」，返回业务数据 data，
//     因此业务层无需再写 res.data.xxx，直接使用 res.xxx 即可。
//   - 业务失败（code!==200）与 HTTP 错误统一 reject 为 Error(message)，
//     错误提示由各视图 catch 后自行弹出，避免重复 toast。
//   - 会话过期（业务码 401 或 HTTP 401）时清空本地凭证并跳转登录页。

import axios from 'axios'

// 认证相关请求路径：这些接口的业务失败（401/400）不应触发"会话过期"重定向
const AUTH_PATHS = ['/auth/login', '/auth/register']

// 会话过期：清空本地凭证，携带当前地址跳转登录页（重登后回到原页面）
function redirectToLogin() {
  if (window.location.pathname === '/login') return
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  const redirect = encodeURIComponent(window.location.pathname + window.location.search)
  window.location.href = `/login?redirect=${redirect}`
}

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

// --- 响应拦截器：统一解包业务数据、处理错误 ---
request.interceptors.response.use(
  // 成功响应处理（HTTP 层成功，业务层可能仍有 code 错误）
  response => {
    const body = response.data
    // 业务成功：直接解包返回 data
    if (body && body.code === 200) return body.data

    const url = response.config.url || ''
    // 业务码 401 且非认证请求 → 会话过期，清凭证并跳登录
    if (body?.code === 401 && !AUTH_PATHS.some(p => url.includes(p))) {
      redirectToLogin()
      return Promise.reject(new Error(body.message || '登录已过期，请重新登录'))
    }
    // 其余业务失败（含登录/注册失败）：仅 reject，由视图统一提示
    return Promise.reject(new Error(body?.message || '请求失败'))
  },
  // HTTP 层错误处理
  error => {
    // HTTP 401：JWT 认证失败，会话过期
    if (error.response?.status === 401) redirectToLogin()
    const msg = error.response?.data?.message || error.message || '请求失败'
    return Promise.reject(new Error(msg))
  }
)

// ==================== 认证相关 API ====================

export const authAPI = {
  // 用户登录（成功返回 { token, user }）
  login: (username, password) => request.post('/auth/login', { username, password }),
  // 用户注册（成功返回 { id, username }）
  register: (username, password) => request.post('/auth/register', { username, password })
}

// ==================== 图书管理 API ====================

export const bookAPI = {
  // 获取图书列表（分页，返回 { records, total, page, pageSize }）
  getAll: (page = 1, pageSize = 10) => request.get('/books', { params: { page, pageSize } }),
  // 获取全部图书（不分页，返回数组，用于书架分类展示）
  getAllFlat: () => request.get('/books/all'),
  // 搜索图书（分页）
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
  // filters 支持 { title, username }，为空则不参与筛选
  getRecords: (page = 1, pageSize = 10, filters = {}) => {
    const params = { page, pageSize }
    if (filters.title) params.title = filters.title
    if (filters.username) params.username = filters.username
    return request.get('/borrow', { params })
  },
  // 归还图书：传入借阅记录 ID
  returnBook: (recordId) => request.post('/borrow/return', { recordId })
}

// ==================== 用户管理 API ====================

export const userAPI = {
  // 获取所有普通用户列表（返回数组）
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
