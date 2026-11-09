// 路由配置文件 - 定义应用的页面路由和导航守卫
//
// 设计约定：
//   - 所有页面组件懒加载（按需分包，减小首屏体积）
//   - 每条路由通过 meta.title 提供页面标题，由 afterEach 动态设置 document.title
//   - 权限守卫用返回值式写法（返回 false / 路由对象 / true）
//   - 需登录的页面在守卫中携带 redirect，登录成功后跳回原页面

import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

// 路由配置表：每个路径对应一个页面组件
const routes = [
  // 首页 - 公开访问
  { path: '/', name: 'Home', component: () => import('../views/Home.vue'), meta: { title: '首页' } },
  // 图书列表页 - 公开访问
  { path: '/books', name: 'Books', component: () => import('../views/Books.vue'), meta: { title: '图书书架' } },
  // 登录页 - 公开访问
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue'), meta: { title: '登录' } },
  // 注册页 - 公开访问
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue'), meta: { title: '注册' } },
  {
    path: '/borrow-records',
    name: 'BorrowRecords',
    component: () => import('../views/BorrowRecords.vue'),
    // meta 元信息：用于路由守卫的权限判断
    meta: { title: '借阅记录', requiresAuth: true } // 需要登录才能访问
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/Admin.vue'),
    // 需要登录 + 管理员角色
    meta: { title: '后台管理', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/accounts',
    name: 'AdminAccounts',
    component: () => import('../views/AdminAccounts.vue'),
    // 需要登录 + 管理员角色
    meta: { title: '账号管理', requiresAuth: true, requiresAdmin: true }
  },
  {
    // 404 兜底：匹配所有未定义路径
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

// 创建路由器实例，使用 HTML5 History 模式（URL 中不带 #）
const router = createRouter({
  history: createWebHistory(),
  routes,
  // 路由切换后回到页面顶部；若目标带锚点则平滑滚动到对应元素
  scrollBehavior(to) {
    if (to.hash) return { el: to.hash, behavior: 'smooth' }
    return { top: 0 }
  }
})

// 全局前置导航守卫：在每次路由跳转前检查权限
// 使用返回值式写法：返回 false 阻止跳转，返回路由对象则重定向，返回 true 放行
router.beforeEach((to) => {
  const auth = useAuthStore()
  // 需要登录但未登录 → 重定向登录页，并携带当前地址用于登录后跳回
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }
  // 需要管理员权限但角色不符 → 重定向首页
  if (to.meta.requiresAdmin && auth.user?.role !== 'admin') {
    return { name: 'Home' }
  }
  return true
})

// 全局后置守卫：根据当前路由设置浏览器标题
router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · 图书管理系统` : '图书管理系统'
})

export default router
