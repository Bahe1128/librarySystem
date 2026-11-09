<template>
  <div class="not-found">
    <div class="code">404</div>
    <div class="icon">
      <el-icon :size="32"><Compass /></el-icon>
    </div>
    <h3>页面不存在</h3>
    <p>你访问的页面不存在或已被移除</p>
    <router-link to="/" class="back-btn">
      <el-icon :size="15"><Back /></el-icon>
      返回首页
    </router-link>
  </div>
</template>

<script setup>
// 404 页面 - 访问不存在的路由时展示
// 由路由 catch-all 规则捕获（见 router/index.js 的 NotFound 路由）
</script>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 96px 20px;
}

.code {
  font-size: 88px;
  font-weight: 800;
  letter-spacing: -0.04em;
  line-height: 1;
  background: linear-gradient(135deg, var(--color-primary-400), var(--color-primary-600));
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin-bottom: 8px;
}

.icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--color-primary-50);
  color: var(--color-primary-600);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}

html.dark .icon {
  background: rgba(124, 90, 228, 0.12);
  color: var(--color-primary-400);
}

.not-found h3 {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
  margin-bottom: 8px;
  color: var(--color-text);
}

.not-found p {
  font-size: 15px;
  color: var(--color-text-secondary);
  margin-bottom: 32px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 11px 26px;
  border-radius: var(--radius-lg);
  background: var(--color-primary);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: all var(--transition-fast);
  box-shadow: 0px 1px 2px rgba(107, 70, 212, 0.24), 0px 4px 12px rgba(107, 70, 212, 0.12);
}

.back-btn:hover {
  background: var(--color-primary-hover);
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0px 2px 4px rgba(107, 70, 212, 0.28), 0px 8px 16px rgba(107, 70, 212, 0.16);
}
</style>
