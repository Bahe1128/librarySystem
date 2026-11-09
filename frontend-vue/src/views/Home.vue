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
          <el-icon :size="18"><Search /></el-icon>
          浏览图书
        </router-link>
        <router-link v-if="!auth.isLoggedIn" to="/login" class="btn-hero-secondary">
          立即登录
          <el-icon :size="16"><ArrowRight /></el-icon>
        </router-link>
      </div>
    </section>

    <section class="stats-row" v-if="stats">
      <div class="stat-card">
        <div class="stat-icon-wrapper stat-icon-books">
          <el-icon :size="22"><Reading /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalBooks || 0 }}</span>
          <span class="stat-label">馆藏图书</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon-wrapper stat-icon-borrow">
          <el-icon :size="22"><Tickets /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.borrowedBooks || 0 }}</span>
          <span class="stat-label">借出中</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon-wrapper stat-icon-overdue">
          <el-icon :size="22"><WarningFilled /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.overdueBooks || 0 }}</span>
          <span class="stat-label">逾期未还</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon-wrapper stat-icon-users">
          <el-icon :size="22"><User /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalUsers || 0 }}</span>
          <span class="stat-label">注册用户</span>
        </div>
      </div>
    </section>

    <section class="features">
      <div class="section-header">
        <h2 class="section-title">核心功能</h2>
        <p class="section-subtitle">覆盖图书管理全流程的强大功能</p>
      </div>
      <div class="feature-grid">
        <div class="feature-card" v-for="f in features" :key="f.title">
          <div class="feature-icon" :class="f.color">
            <el-icon :size="22"><component :is="f.icon" /></el-icon>
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
// 注意：stats 接口需要登录，未登录访客不请求（避免被 401 重定向踢回登录页）
// 即使获取失败也不影响页面正常展示（统计数据为可选功能）
onMounted(async () => {
  if (!auth.isLoggedIn) return
  try {
    // API 拦截器已解包，res 即统计对象
    stats.value = await statsAPI.getStats()
  } catch (_) {
    // stats 数据获取失败时静默处理，不影响页面渲染
  }
})
</script>

<style scoped>
.home { max-width: 960px; margin: 0 auto; }

/* ===== Hero ===== */
.hero {
  text-align: center;
  padding: 72px 0 56px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 5px 14px;
  border-radius: var(--radius-full);
  background: var(--color-primary-50);
  color: var(--color-primary-700);
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 24px;
  border: 1px solid var(--color-primary-100);
}

html.dark .hero-badge {
  background: rgba(124, 90, 228, 0.12);
  color: var(--color-primary-300);
  border-color: rgba(124, 90, 228, 0.18);
}

.badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary-500);
  box-shadow: 0 0 0 2px rgba(124, 90, 228, 0.20);
}

.hero-title {
  font-size: 52px;
  font-weight: 700;
  letter-spacing: -0.03em;
  line-height: 1.12;
  color: var(--color-text);
  margin-bottom: 16px;
}

.hero-desc {
  font-size: 18px;
  color: var(--color-text-secondary);
  max-width: 540px;
  margin: 0 auto 36px;
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
  padding: 13px 30px;
  border-radius: var(--radius-lg);
  background: var(--color-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  text-decoration: none;
  transition: all var(--transition);
  letter-spacing: -0.01em;
  box-shadow: 0px 1px 2px rgba(107, 70, 212, 0.24), 0px 4px 12px rgba(107, 70, 212, 0.12);
}

.btn-hero-primary:hover {
  background: var(--color-primary-hover);
  transform: translateY(-1px);
  box-shadow: 0px 2px 4px rgba(107, 70, 212, 0.28), 0px 8px 20px rgba(107, 70, 212, 0.16);
  color: #fff;
}

.btn-hero-secondary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 13px 28px;
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: 15px;
  font-weight: 600;
  text-decoration: none;
  border: 1px solid var(--color-border);
  transition: all var(--transition);
  letter-spacing: -0.01em;
}

.btn-hero-secondary:hover {
  border-color: var(--color-border-hover);
  background: var(--gray-50);
  color: var(--color-text);
  box-shadow: var(--shadow-xs);
}

/* ===== Stats ===== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 64px;
}

@media (max-width: 1024px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 640px) {
  .stats-row { grid-template-columns: 1fr; }
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 24px 28px;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border);
  transition: all var(--transition);
}

.stat-card:hover {
  box-shadow: var(--shadow-lg);
  border-color: transparent;
  transform: translateY(-2px);
}

.stat-icon-wrapper {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon-books   { background: var(--color-primary-50); color: var(--color-primary-600); }
.stat-icon-borrow  { background: var(--color-success-50); color: var(--color-success-600); }
.stat-icon-overdue { background: var(--color-danger-50); color: var(--color-danger-600); }
.stat-icon-users   { background: var(--color-warning-50); color: var(--color-warning-600); }

html.dark .stat-icon-books   { background: rgba(124,90,228,0.12); color: var(--color-primary-400); }
html.dark .stat-icon-borrow  { background: rgba(16,185,129,0.12); color: var(--color-success-400); }
html.dark .stat-icon-overdue { background: rgba(239,68,68,0.12); color: var(--color-danger-400); }
html.dark .stat-icon-users   { background: rgba(245,158,11,0.12); color: var(--color-warning-400); }

.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 28px; font-weight: 700; letter-spacing: -0.02em; line-height: 1.2; font-variant-numeric: tabular-nums; }
.stat-label { font-size: 14px; color: var(--color-text-secondary); margin-top: 2px; font-weight: 500; }

/* ===== Features ===== */
.features { padding-bottom: 32px; }

.section-header {
  text-align: center;
  margin-bottom: 40px;
}

.section-title {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.02em;
  margin-bottom: 8px;
  color: var(--color-text);
}

.section-subtitle {
  font-size: 16px;
  color: var(--color-text-secondary);
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.feature-card {
  padding: 32px 28px;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border);
  transition: all var(--transition);
}

.feature-card:hover {
  box-shadow: var(--shadow-xl);
  border-color: transparent;
  transform: translateY(-3px);
}

.feature-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.feature-icon.violet  { background: var(--color-primary-50);  color: var(--color-primary-600); }
.feature-icon.emerald { background: var(--color-success-50);  color: var(--color-success-600); }
.feature-icon.amber   { background: var(--color-warning-50);  color: var(--color-warning-600); }
.feature-icon.rose    { background: var(--color-danger-50);   color: var(--color-danger-600); }
.feature-icon.sky     { background: #f0f9ff; color: #0284c7; }
.feature-icon.slate   { background: #f8fafc; color: #475569; }

html.dark .feature-icon.violet  { background: rgba(124,90,228,0.12);  color: var(--color-primary-400); }
html.dark .feature-icon.emerald { background: rgba(16,185,129,0.12);  color: var(--color-success-400); }
html.dark .feature-icon.amber   { background: rgba(245,158,11,0.12);  color: var(--color-warning-400); }
html.dark .feature-icon.rose    { background: rgba(239,68,68,0.12);   color: var(--color-danger-400); }
html.dark .feature-icon.sky     { background: rgba(56,189,248,0.12);  color: #38bdf8; }
html.dark .feature-icon.slate   { background: rgba(148,163,184,0.12); color: #94a3b8; }

.feature-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  letter-spacing: -0.01em;
  color: var(--color-text);
}

.feature-desc {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.65;
}

/* ===== 入场动画：统计卡与功能卡依次浮现 ===== */
@keyframes fade-up {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.feature-card,
.stat-card {
  animation: fade-up 0.5s var(--transition) both;
}
.stat-card:nth-child(1) { animation-delay: 0ms; }
.stat-card:nth-child(2) { animation-delay: 60ms; }
.stat-card:nth-child(3) { animation-delay: 120ms; }
.stat-card:nth-child(4) { animation-delay: 180ms; }
.feature-card:nth-child(1) { animation-delay: 0ms; }
.feature-card:nth-child(2) { animation-delay: 60ms; }
.feature-card:nth-child(3) { animation-delay: 120ms; }
.feature-card:nth-child(4) { animation-delay: 180ms; }
.feature-card:nth-child(5) { animation-delay: 240ms; }
.feature-card:nth-child(6) { animation-delay: 300ms; }

@media (max-width: 768px) {
  .hero {
    padding: 40px 0 36px;
  }
  .hero-title { font-size: 32px; }
  .hero-desc { font-size: 16px; }
  .feature-grid { grid-template-columns: 1fr; }
  .hero-actions { flex-direction: column; align-items: center; }
}
</style>
