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
    return data
  },
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export const authAPI = {
  login: (username, password) => request.post('/auth/login', { username, password }),
  register: (username, password) => request.post('/auth/register', { username, password })
}

export const bookAPI = {
  getAll: (page = 1, pageSize = 10) => request.get('/books', { params: { page, pageSize } }),
  search: (keyword, page = 1, pageSize = 10) => request.get('/books/search', { params: { keyword, page, pageSize } }),
  getById: (id) => request.get(`/books/${id}`),
  create: (data) => request.post('/books', data),
  update: (id, data) => request.put(`/books/${id}`, data),
  delete: (id) => request.delete(`/books/${id}`)
}

export const borrowAPI = {
  borrow: (bookId) => request.post('/borrow', { bookId }),
  getRecords: (page = 1, pageSize = 10) => request.get('/borrow', { params: { page, pageSize } }),
  returnBook: (recordId) => request.post('/borrow/return', { recordId })
}

export const userAPI = {
  getAll: () => request.get('/users'),
  getById: (id) => request.get(`/users/${id}`),
  create: (data) => request.post('/users', data),
  update: (id, data) => request.put(`/users/${id}`, data),
  delete: (id) => request.delete(`/users/${id}`)
}

export const statsAPI = {
  getStats: () => request.get('/stats'),
  getTrends: () => request.get('/stats/trends')
}
