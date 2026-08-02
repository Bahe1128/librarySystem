<template>
  <header class="navbar">
    <div class="navbar-inner">
      <router-link to="/" class="brand">
        <span class="brand-icon">
          <el-icon :size="20"><Reading /></el-icon>
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
                <el-icon :size="14" class="chevron"><ArrowDown /></el-icon>
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

        <el-tooltip :content="theme.isDark ? '切换日间模式' : '切换夜间模式'" placement="bottom-end">
          <button class="theme-toggle" @click="theme.toggle()" aria-label="切换日间/夜间模式">
            <el-icon :size="18">
              <Sunny v-if="!theme.isDark" />
              <Moon v-else />
            </el-icon>
          </button>
        </el-tooltip>
      </div>
    </div>
  </header>
</template>

<script setup>
// 导航栏组件 - 应用顶部的全局导航
// 根据用户登录状态和角色动态显示不同的导航菜单项
// 登录后显示用户头像和退出登录按钮，未登录显示登录/注册按钮

// 引入认证 Store 获取用户状态
import { useAuthStore } from '../stores/auth'
// 引入主题 Store 控制日间/夜间模式
import { useThemeStore } from '../stores/theme'
const auth = useAuthStore()
const theme = useThemeStore()
</script>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  border-bottom: 1px solid var(--color-border);
}

html.dark .navbar {
  background: rgba(12, 14, 18, 0.80);
}

.navbar-inner {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 68px;
  padding: 0 32px;
  gap: 36px;
}

/* Brand */
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--color-text);
  font-weight: 700;
  font-size: 17px;
  flex-shrink: 0;
  letter-spacing: -0.01em;
}

.brand-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  background: var(--color-primary);
  color: #fff;
  border-radius: var(--radius-lg);
  box-shadow: 0 1px 3px rgba(107, 70, 212, 0.30), 0 4px 10px rgba(107, 70, 212, 0.12);
}

/* Nav Links */
.nav-links {
  display: flex;
  align-items: center;
  gap: 2px;
  flex: 1;
}

.nav-link {
  padding: 8px 14px;
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: all var(--transition-fast);
}

.nav-link:hover {
  color: var(--color-text);
  background: var(--color-border-light);
}

.nav-link.active {
  color: var(--color-primary);
  background: var(--color-primary-50);
}

html.dark .nav-link.active {
  background: rgba(124, 90, 228, 0.10);
}

/* Actions */
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
  transition: all var(--transition-fast);
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  letter-spacing: -0.01em;
}

.btn-primary {
  background: var(--color-primary);
  color: #fff;
  box-shadow: 0px 1px 2px rgba(107, 70, 212, 0.24);
}

.btn-primary:hover {
  background: var(--color-primary-hover);
  color: #fff;
  box-shadow: 0px 1px 3px rgba(107, 70, 212, 0.30);
}

.btn-ghost {
  background: transparent;
  color: var(--color-text-secondary);
}

.btn-ghost:hover {
  background: var(--color-border-light);
  color: var(--color-text);
}

/* User */
.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 3px 12px 3px 3px;
  border: none;
  border-radius: var(--radius-full);
  background: var(--color-border-light);
  cursor: pointer;
  font-family: inherit;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  transition: all var(--transition-fast);
  letter-spacing: -0.01em;
}

.user-trigger:hover {
  background: var(--color-border);
}

.chevron {
  color: var(--color-text-tertiary);
  transition: transform var(--transition-fast);
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary-500), var(--color-primary-600));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.15);
}

.username {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Theme Toggle */
.theme-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--color-text-tertiary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.theme-toggle:hover {
  background: var(--color-border-light);
  color: var(--color-text);
}

@media (max-width: 768px) {
  .navbar-inner {
    padding: 0 16px;
    gap: 16px;
  }
  .nav-links { display: none; }
}
</style>
