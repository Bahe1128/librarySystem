<template>
  <div class="empty-state">
    <div class="empty-icon">
      <el-icon :size="40"><component :is="icon" /></el-icon>
    </div>
    <h3>{{ title }}</h3>
    <p v-if="desc">{{ desc }}</p>
    <slot />
  </div>
</template>

<script setup>
// 空状态组件 - 统一"无数据"展示
// icon 传入全局注册的 Element Plus 图标组件名（字符串）
defineProps({
  icon: { type: String, default: 'FolderOpened' },
  title: { type: String, default: '暂无数据' },
  desc: { type: String, default: '' }
})
</script>

<style scoped>
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: var(--color-text-secondary);
}

.empty-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--color-border-light);
  color: var(--color-text-tertiary);
  margin-bottom: 20px;
}

.empty-state h3 {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 6px;
  color: var(--color-text);
}

.empty-state p { font-size: 14px; }
</style>
