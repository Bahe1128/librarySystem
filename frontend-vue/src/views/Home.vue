<template>
  <div class="home">
    <section class="hero">
      <div class="hero-badge">
        <span class="badge-dot"></span>
        现代化图书管理体验
      </div>
      <h1 class="hero-title">轻松管理每一本书</h1>
      <p class="hero-desc">支持图书的添加、编辑、查询与借阅归还，让图书馆运营井然有序。</p>
      <div class="hero-actions">
        <router-link to="/books" class="btn-hero-primary">
          <el-icon><Search /></el-icon>
          浏览图书
        </router-link>
        <router-link v-if="!auth.isLoggedIn" to="/login" class="btn-hero-ghost">
          立即登录
          <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>
    </section>

    <section class="stats-row" v-if="stats">
      <div class="stat-card">
        <div class="stat-icon books">
          <el-icon :size="22"><Reading /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalBooks || 0 }}</span>
          <span class="stat-label">馆藏图书</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon borrow">
          <el-icon :size="22"><Tickets /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.activeBorrows || 0 }}</span>
          <span class="stat-label">借出中</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon users">
          <el-icon :size="22"><User /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalUsers || 0 }}</span>
          <span class="stat-label">注册用户</span>
        </div>
      </div>
    </section>

    <section class="features">
      <h2 class="section-title">核心功能</h2>
      <div class="feature-grid">
        <div class="feature-card" v-for="f in features" :key="f.title">
          <div class="feature-icon" :class="f.color">
            <el-icon :size="24"><component :is="f.icon" /></el-icon>
          </div>
          <h3 class="feature-title">{{ f.title }}</h3>
          <p class="feature-desc">{{ f.desc }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
// 首页 - 应用的欢迎页和功能展示
// 展示统计数据（馆藏数量、借阅数量、注册用户数）和核心功能介绍

import { ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { statsAPI } from '../api'

const auth = useAuthStore()
// 统计数据对象（totalBooks, activeBorrows, totalUsers）
const stats = ref(null)

// 核心功能列表：用于渲染功能介绍卡片
const features = [
  { icon: 'Reading', title: '图书管理', desc: '添加、编辑、删除和搜索图书信息，支持多种查询方式', color: 'violet' },
  { icon: 'Tickets', title: '借阅管理', desc: '一键借阅与归还，完整记录每本书的流转历史', color: 'emerald' },
  { icon: 'DataAnalysis', title: '数据统计', desc: '直观的数据看板，掌握馆藏和借阅趋势', color: 'amber' },
  { icon: 'Lock', title: '权限控制', desc: 'JWT 安全认证，管理员与普通用户权限分离', color: 'rose' },
  { icon: 'Monitor', title: '响应式设计', desc: '适配各类设备屏幕，随时随地管理图书', color: 'sky' },
  { icon: 'Setting', title: '易于扩展', desc: 'Spring Boot + Vue 3 技术栈，维护方便', color: 'slate' },
]

// 组件挂载后获取统计数据
// 注意：即使获取失败也不影响页面正常展示（统计数据为可选功能）
onMounted(async () => {
  try {
    const res = await statsAPI.getStats()
    // 将返回的统计数据赋值
    stats.value = res.data
  } catch (_) {
    // stats 数据获取失败时静默处理，不影响页面渲染
  }
})
</script>

<style scoped>
.home { max-width: 960px; margin: 0 auto; }

/* Hero */
.hero {
  text-align: center;
  padding: 64px 0 48px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-radius: 999px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 24px;
}

.badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary);
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  letter-spacing: -0.03em;
  line-height: 1.15;
  color: var(--color-text);
  margin-bottom: 16px;
}

.hero-desc {
  font-size: 18px;
  color: var(--color-text-secondary);
  max-width: 520px;
  margin: 0 auto 32px;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.btn-hero-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  border-radius: var(--radius-lg);
  background: var(--color-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  text-decoration: none;
  transition: all var(--transition);
  box-shadow: 0 1px 3px rgba(124, 58, 237, 0.3), 0 4px 12px rgba(124, 58, 237, 0.15);
}

.btn-hero-primary:hover {
  background: var(--color-primary-hover);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(124, 58, 237, 0.35), 0 8px 20px rgba(124, 58, 237, 0.2);
  color: #fff;
}

.btn-hero-ghost {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 12px 28px;
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: 15px;
  font-weight: 600;
  text-decoration: none;
  border: 1px solid var(--color-border);
  transition: all var(--transition);
}

.btn-hero-ghost:hover {
  border-color: var(--color-text-tertiary);
  background: var(--color-bg);
  color: var(--color-text);
}

/* Stats */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 64px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  transition: all var(--transition);
}

.stat-card:hover {
  box-shadow: var(--shadow-md);
  border-color: transparent;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon.books { background: var(--color-primary-bg); color: var(--color-primary); }
.stat-icon.borrow { background: var(--color-success-light); color: var(--color-success); }
.stat-icon.users { background: var(--color-warning-light); color: var(--color-warning); }

.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 24px; font-weight: 700; letter-spacing: -0.02em; }
.stat-label { font-size: 13px; color: var(--color-text-secondary); margin-top: 2px; }

/* Features */
.features { padding-bottom: 16px; }

.section-title {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
  margin-bottom: 24px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.feature-card {
  padding: 28px 24px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  transition: all var(--transition);
}

.feature-card:hover {
  box-shadow: var(--shadow-lg);
  border-color: transparent;
  transform: translateY(-2px);
}

.feature-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 18px;
}

.feature-icon.violet { background: var(--color-primary-bg); color: var(--color-primary); }
.feature-icon.emerald { background: var(--color-success-light); color: var(--color-success); }
.feature-icon.amber { background: var(--color-warning-light); color: var(--color-warning); }
.feature-icon.rose { background: var(--color-danger-light); color: var(--color-danger); }
.feature-icon.sky { background: #e0f2fe; color: #0284c7; }
.feature-icon.slate { background: #f1f5f9; color: #475569; }

.feature-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 6px;
}

.feature-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.65;
}

@media (max-width: 768px) {
  .hero-title { font-size: 32px; }
  .stats-row { grid-template-columns: 1fr; }
  .feature-grid { grid-template-columns: 1fr; }
  .hero-actions { flex-direction: column; align-items: center; }
}
</style>
