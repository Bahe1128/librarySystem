import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import Home from '../views/Home.vue'
import Books from '../views/Books.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import BorrowRecords from '../views/BorrowRecords.vue'
import Admin from '../views/Admin.vue'

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/books', name: 'Books', component: Books },
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  {
    path: '/borrow-records',
    name: 'BorrowRecords',
    component: BorrowRecords,
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: Admin,
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    next({ name: 'Login' })
  } else if (to.meta.requiresAdmin && auth.user?.role !== 'admin') {
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
