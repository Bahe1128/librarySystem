// 路由配置文件 - 定义应用的页面路由和导航守卫

import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

// 按需懒加载页面组件（这里直接导入）
import Home from '../views/Home.vue'
import Books from '../views/Books.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import BorrowRecords from '../views/BorrowRecords.vue'
import Admin from '../views/Admin.vue'
import AdminAccounts from '../views/AdminAccounts.vue'

// 路由配置表：每个路径对应一个页面组件
const routes = [
  // 首页 - 公开访问
  { path: '/', name: 'Home', component: Home },
  // 图书列表页 - 公开访问
  { path: '/books', name: 'Books', component: Books },
  // 登录页 - 公开访问
  { path: '/login', name: 'Login', component: Login },
  // 注册页 - 公开访问
  { path: '/register', name: 'Register', component: Register },
  {
    path: '/borrow-records',
    name: 'BorrowRecords',
    component: BorrowRecords,
    // meta 元信息：用于路由守卫的权限判断
    meta: { requiresAuth: true } // 需要登录才能访问
  },
  {
    path: '/admin',
    name: 'Admin',
    component: Admin,
    // 需要登录 + 管理员角色
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/accounts',
    name: 'AdminAccounts',
    component: AdminAccounts,
    // 需要登录 + 管理员角色
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

// 创建路由器实例，使用 HTML5 History 模式（URL 中不带 #）
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置导航守卫：在每次路由跳转前检查权限
// to: 目标路由, from: 来源路由, next: 放行函数
router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  // 如果目标路由需要登录但用户未登录 → 重定向到登录页
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    next({ name: 'Login' })
  // 如果目标路由需要管理员权限但用户不是 admin → 重定向到首页
  } else if (to.meta.requiresAdmin && auth.user?.role !== 'admin') {
    next({ name: 'Home' })
  } else {
    // 放行
    next()
  }
})

export default router
