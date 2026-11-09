<template>
  <div class="table-card">
    <slot />
  </div>
</template>

<script setup>
// 表格卡片容器 - 圆角卡片 + 边框 + 阴影，统一包裹 el-table 等表格内容
</script>

<style scoped>
.table-card {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border);
  overflow: hidden;
  box-shadow: var(--shadow-xs);
}
</style>
