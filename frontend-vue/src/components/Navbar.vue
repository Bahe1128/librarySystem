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

        <!-- 移动端菜单按钮：桌面端隐藏 -->
        <button class="hamburger" @click="drawerOpen = true" aria-label="打开导航菜单">
          <el-icon :size="20"><Menu /></el-icon>
        </button>
      </div>
    </div>
  </header>

  <!-- 移动端抽屉导航 -->
  <el-drawer v-model="drawerOpen" direction="rtl" size="280px" :with-header="false">
    <div class="drawer-body">
      <router-link to="/" class="drawer-brand" @click="drawerOpen = false">
        <span class="brand-icon">
          <el-icon :size="18"><Reading /></el-icon>
        </span>
        <span class="brand-text">图书管理系统</span>
      </router-link>

      <nav class="drawer-nav">
        <router-link
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="drawer-link"
          active-class="active"
          @click="drawerOpen = false"
        >
          <el-icon :size="17" class="drawer-link-icon"><component :is="item.icon" /></el-icon>
          {{ item.label }}
        </router-link>
      </nav>

      <div class="drawer-footer">
        <template v-if="auth.isLoggedIn">
          <div class="drawer-user">
            <span class="avatar">{{ auth.user?.username?.charAt(0)?.toUpperCase() }}</span>
            <span class="username">{{ auth.user?.username }}</span>
          </div>
          <button class="drawer-logout" @click="handleLogout">
            <el-icon :size="15"><SwitchButton /></el-icon>
            退出登录
          </button>
        </template>
        <template v-else>
          <div class="drawer-auth">
            <router-link to="/login" class="btn btn-ghost" @click="drawerOpen = false">登录</router-link>
            <router-link to="/register" class="btn btn-primary" @click="drawerOpen = false">注册</router-link>
          </div>
        </template>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
// 导航栏组件 - 应用顶部的全局导航
// 根据用户登录状态和角色动态显示不同的导航菜单项
// 登录后显示用户头像和退出登录按钮，未登录显示登录/注册按钮
// 移动端（<768px）隐藏顶部链接，改用汉堡按钮打开抽屉菜单

import { ref, computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useThemeStore } from '../stores/theme'
const auth = useAuthStore()
const theme = useThemeStore()

// 抽屉是否打开
const drawerOpen = ref(false)

// 抽屉导航项（根据登录状态与角色动态生成）
const navItems = computed(() => {
  const items = [
    { to: '/', label: '首页', icon: 'HomeFilled' },
    { to: '/books', label: '图书列表', icon: 'Reading' }
  ]
  if (auth.isLoggedIn) items.push({ to: '/borrow-records', label: '借阅记录', icon: 'Tickets' })
  if (auth.user?.role === 'admin') {
    items.push(
      { to: '/admin', label: '后台管理', icon: 'Setting' },
      { to: '/admin/accounts', label: '账号管理', icon: 'User' }
    )
  }
  return items
})

// 退出登录并关闭抽屉
function handleLogout() {
  auth.logout()
  drawerOpen.value = false
}
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

/* Hamburger (mobile only) */
.hamburger {
  display: none;
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

.hamburger:hover {
  background: var(--color-border-light);
  color: var(--color-text);
}

/* ===== Drawer ===== */
.drawer-body {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 8px 4px;
}

.drawer-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--color-text);
  font-weight: 700;
  font-size: 16px;
  letter-spacing: -0.01em;
  padding: 8px 12px 20px;
  border-bottom: 1px solid var(--color-border-light);
  margin-bottom: 16px;
}

.drawer-brand .brand-icon {
  width: 34px;
  height: 34px;
}

.drawer-nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.drawer-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 14px;
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: all var(--transition-fast);
}

.drawer-link:hover {
  color: var(--color-text);
  background: var(--color-border-light);
}

.drawer-link.active {
  color: var(--color-primary);
  background: var(--color-primary-50);
}

html.dark .drawer-link.active {
  background: rgba(124, 90, 228, 0.10);
}

.drawer-link-icon {
  color: var(--color-text-tertiary);
}

.drawer-footer {
  padding: 16px 0 8px;
  border-top: 1px solid var(--color-border-light);
}

.drawer-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px 14px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}

.drawer-logout {
  width: 100%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px;
  border: none;
  border-radius: var(--radius-md);
  background: var(--color-danger-50);
  color: var(--color-danger-600);
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.drawer-logout:hover {
  background: var(--color-danger-100);
}

.drawer-auth {
  display: flex;
  gap: 10px;
}

.drawer-auth .btn {
  flex: 1;
  justify-content: center;
  padding: 10px;
}

@media (max-width: 768px) {
  .navbar-inner {
    padding: 0 16px;
    gap: 16px;
  }
  .nav-links { display: none; }
  .hamburger { display: flex; }
}
</style>
