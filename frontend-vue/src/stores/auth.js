// 认证状态管理 (Pinia Store)
// Pinia 是 Vue 3 官方推荐的状态管理库，用于在组件之间共享状态
// 此 Store 负责管理用户的登录状态、Token 和用户信息

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authAPI } from '../api'

// 使用 Composition API 风格定义 Store（setup 语法）
// 第一个参数 'auth' 是 Store 的唯一标识名
export const useAuthStore = defineStore('auth', () => {
  // --- 响应式状态 (state) ---

  // JWT Token：从本地存储中恢复，实现页面刷新后保持登录状态
  const token = ref(localStorage.getItem('token') || '')
  // 当前用户信息对象：从本地存储中恢复
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  // --- 计算属性 (getters) ---

  // 是否已登录：根据 token 是否存在来判断
  const isLoggedIn = computed(() => !!token.value)

  // --- 操作方法 (actions) ---

  // 设置认证信息：保存 token 和用户信息到内存和本地存储
  function setAuth(newToken, newUser) {
    token.value = newToken
    user.value = newUser
    // 持久化到 localStorage，确保刷新页面不丢失登录状态
    localStorage.setItem('token', newToken)
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  // 退出登录：清除所有认证信息
  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  // 登录操作：调用后端 API 进行认证，成功后将结果保存
  async function login(username, password) {
    const res = await authAPI.login(username, password)
    // 调用 setAuth 保存登录凭证
    setAuth(res.data.token, res.data.user)
    return res.data
  }

  // 注册操作：调用后端 API 注册新用户（注册后需手动登录）
  async function register(username, password) {
    return await authAPI.register(username, password)
  }

  // 导出需要暴露给外部使用的状态和方法
  return { token, user, isLoggedIn, login, register, logout, setAuth }
})
