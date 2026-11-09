<template>
  <div class="page-header">
    <div class="page-header-text">
      <h2 class="page-title">{{ title }}</h2>
      <p v-if="desc" class="page-desc">{{ desc }}</p>
    </div>
    <div class="page-header-actions">
      <slot />
    </div>
  </div>
</template>

<script setup>
// 页头组件 - 统一页面标题、描述与右侧操作区
// 右侧操作区通过默认插槽传入（如"添加图书"按钮），没有则自动省略
defineProps({
  title: { type: String, required: true },
  desc: { type: String, default: '' }
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 28px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.03em;
  margin-bottom: 6px;
  color: var(--color-text);
}

.page-desc {
  font-size: 15px;
  color: var(--color-text-secondary);
}

@media (max-width: 640px) {
  .page-header { flex-direction: column; }
}
</style>
