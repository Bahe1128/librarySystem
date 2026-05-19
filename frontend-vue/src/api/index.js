import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
      return Promise.reject(new Error(data.message))
    }
    if (data.code !== 200) {
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    return data
  },
  error => {
    return Promise.reject(error)
  }
)

export const authAPI = {
  login: (username, password) => request.post('/auth/login', { username, password }),
  register: (username, password) => request.post('/auth/register', { username, password })
}

export const bookAPI = {
  getAll: () => request.get('/books'),
  search: (keyword) => request.get('/books/search', { params: { keyword } }),
  getById: (id) => request.get(`/books/${id}`),
  create: (data) => request.post('/books', data),
  update: (id, data) => request.put(`/books/${id}`, data),
  delete: (id) => request.delete(`/books/${id}`)
}

export const borrowAPI = {
  borrow: (bookId) => request.post('/borrow', { bookId }),
  getRecords: () => request.get('/borrow'),
  returnBook: (recordId) => request.post('/borrow/return', { recordId })
}

export const statsAPI = {
  getStats: () => request.get('/stats'),
  getTrends: () => request.get('/stats/trends')
}
