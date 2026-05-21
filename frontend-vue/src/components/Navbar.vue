<template>
  <header class="navbar">
    <div class="navbar-inner">
      <router-link to="/" class="brand">
        <span class="brand-icon">
          <el-icon :size="22"><Reading /></el-icon>
        </span>
        <span class="brand-text">图书管理系统</span>
      </router-link>

      <nav class="nav-links">
        <router-link to="/" class="nav-link" active-class="active" exact>首页</router-link>
        <router-link to="/books" class="nav-link" active-class="active">图书列表</router-link>
        <router-link v-if="auth.isLoggedIn" to="/borrow-records" class="nav-link" active-class="active">借阅记录</router-link>
        <router-link v-if="auth.user?.role === 'admin'" to="/admin" class="nav-link" active-class="active">后台管理</router-link>
        <router-link v-if="auth.user?.role === 'admin'" to="/admin/accounts" class="nav-link" active-class="active">账号管理</router-link>
      </nav>

      <div class="nav-actions">
        <template v-if="auth.isLoggedIn">
          <div class="user-menu">
            <el-dropdown trigger="click" placement="bottom-end">
              <button class="user-trigger">
                <span class="avatar">{{ auth.user?.username?.charAt(0)?.toUpperCase() }}</span>
                <span class="username">{{ auth.user?.username }}</span>
                <el-icon :size="14"><ArrowDown /></el-icon>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="auth.logout()">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </template>
        <template v-else>
          <router-link to="/login" class="btn btn-ghost">登录</router-link>
          <router-link to="/register" class="btn btn-primary">注册</router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'
const auth = useAuthStore()
</script>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--color-border);
}

.navbar-inner {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 64px;
  padding: 0 24px;
  gap: 32px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--color-text);
  font-weight: 700;
  font-size: 17px;
  flex-shrink: 0;
}

.brand-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: var(--color-primary);
  color: #fff;
  border-radius: var(--radius-md);
}

.brand-text {
  letter-spacing: -0.01em;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.nav-link {
  padding: 8px 14px;
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: all var(--transition);
}

.nav-link:hover {
  color: var(--color-text);
  background: var(--color-border-light);
}

.nav-link.active {
  color: var(--color-primary);
  background: var(--color-primary-light);
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.btn {
  padding: 8px 18px;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all var(--transition);
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.btn-primary {
  background: var(--color-primary);
  color: #fff;
}

.btn-primary:hover {
  background: var(--color-primary-hover);
  color: #fff;
}

.btn-ghost {
  background: transparent;
  color: var(--color-text-secondary);
}

.btn-ghost:hover {
  background: var(--color-border-light);
  color: var(--color-text);
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 4px;
  border: none;
  border-radius: 999px;
  background: var(--color-bg);
  cursor: pointer;
  font-family: inherit;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  transition: all var(--transition);
}

.user-trigger:hover {
  background: var(--color-border-light);
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
}

.username {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .nav-links { display: none; }
}
</style>
