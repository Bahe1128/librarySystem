<template>
  <div ref="root" class="book-popover" :class="{ above }">
    <div class="po-header">
      <span class="po-title">{{ book.title }}</span>
      <span class="po-badge" :class="availClass">{{ statusText }}</span>
    </div>
    <div class="po-body">
      <div class="po-row"><span class="po-label">作者</span><span class="po-value">{{ book.author || '-' }}</span></div>
      <div class="po-row"><span class="po-label">ISBN</span><span class="po-value mono">{{ book.isbn || '-' }}</span></div>
      <div class="po-row"><span class="po-label">分类</span><span class="po-value">{{ book.category || '未分类' }}</span></div>
      <div class="po-row">
        <span class="po-label">馆藏</span>
        <span class="po-value">
          <b class="po-copies" :class="availClass">{{ book.availableCopies }}</b>
          <span class="po-copies-sep">/</span>
          <span>{{ book.totalCopies }} 本可用</span>
        </span>
      </div>
    </div>
    <div class="po-actions">
      <template v-if="isLoggedIn">
        <button
          class="po-btn po-borrow"
          :class="{ disabled: book.availableCopies <= 0, loading: borrowingId === book.id }"
          :disabled="book.availableCopies <= 0 || borrowingId === book.id"
          @click="$emit('borrow', book)"
        >
          <el-icon v-if="borrowingId === book.id" class="is-loading"><Loading /></el-icon>
          {{ book.availableCopies <= 0 ? '已借完' : '借阅' }}
        </button>
        <button
          v-if="isAdmin"
          class="po-btn po-delete"
          @click="$emit('delete', book)"
        >
          <el-icon :size="14"><Delete /></el-icon> 删除
        </button>
      </template>
      <router-link v-else to="/login" class="po-login">
        登录后可借阅 <el-icon :size="12"><ArrowRight /></el-icon>
      </router-link>
    </div>
  </div>
</template>

<script setup>
// 悬浮详情卡组件 - 展示单本图书详情与借阅/删除操作
// 父级负责定位（通过 fallthrough 的 :style 传入坐标），本组件只负责内容与操作触发
// 通过 defineExpose 暴露根元素，供父级测量卡片尺寸

import { ref, computed } from 'vue'

const props = defineProps({
  book: { type: Object, required: true },
  // 卡片位于书脊上方（true）或下方（false），决定箭头方向
  above: { type: Boolean, default: true },
  // 正在借阅的图书 ID（用于按钮 loading 态）
  borrowingId: { type: [Number, String], default: null },
  // 登录状态与管理员角色（决定操作区展示）
  isLoggedIn: { type: Boolean, default: false },
  isAdmin: { type: Boolean, default: false }
})

defineEmits(['borrow', 'delete'])

// 根元素引用，通过 expose 暴露给父级测量尺寸
// 用 getter 保证父级拿到的是 DOM 节点本身（而非 ref 包装）
const root = ref(null)
defineExpose({
  get el() { return root.value }
})

// 展示辅助
const statusText = computed(() => props.book.availableCopies > 0 ? '可借' : '已借完')
const availClass = computed(() => props.book.availableCopies > 0 ? 'ok' : 'off')
</script>

<style scoped>
.book-popover {
  position: fixed;
  z-index: 200;
  width: 260px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-2xl);
  padding: 18px 20px;
  font-size: 13px;
  color: var(--color-text);
}
.book-popover::after {
  content: '';
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  border: 8px solid transparent;
}
.book-popover.above::after { bottom: -16px; border-top-color: var(--color-surface); }
.book-popover.below::after { top: -16px; border-bottom-color: var(--color-surface); }

.po-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 14px;
}
.po-title { font-size: 15px; font-weight: 700; line-height: 1.4; letter-spacing: -0.01em; color: var(--color-text); }
.po-badge {
  flex-shrink: 0;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: var(--radius-full);
  letter-spacing: -0.01em;
}
.po-badge.ok { background: var(--color-success-50); color: var(--color-success-600); }
.po-badge.off { background: var(--color-border-light); color: var(--color-text-tertiary); }

html.dark .po-badge.ok { background: rgba(16,185,129,0.14); color: var(--color-success-400); }
html.dark .po-badge.off { background: rgba(255,255,255,0.04); color: var(--color-text-tertiary); }

.po-body { margin-bottom: 2px; }

.po-row {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding: 4px 0;
  font-size: 13px;
}
.po-label {
  flex-shrink: 0;
  width: 34px;
  color: var(--color-text-tertiary);
  font-size: 12px;
}
.po-value { color: var(--color-text); word-break: break-all; }
.po-value.mono { font-family: 'SF Mono', 'Fira Code', 'Cascadia Code', monospace; font-size: 12px; }
.po-copies.ok { color: var(--color-success); }
.po-copies.off { color: var(--color-danger); }
.po-copies-sep { color: var(--color-text-tertiary); margin: 0 1px; }

.po-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--color-border-light);
}
.po-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 8px 18px;
  border: none;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition-fast);
  letter-spacing: -0.01em;
}
.po-borrow {
  flex: 1;
  background: var(--color-primary);
  color: #fff;
  box-shadow: 0px 1px 2px rgba(107, 70, 212, 0.20);
}
.po-borrow:hover:not(:disabled) {
  background: var(--color-primary-hover);
  box-shadow: 0px 1px 3px rgba(107, 70, 212, 0.30);
}
.po-borrow.disabled {
  background: var(--color-border-light);
  color: var(--color-text-tertiary);
  cursor: not-allowed;
  box-shadow: none;
}
.po-borrow.loading { opacity: 0.65; }
.po-delete {
  background: transparent;
  color: var(--color-danger);
  border: 1px solid var(--color-danger-100);
}
.po-delete:hover { background: var(--color-danger-50); }
.po-login {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: var(--color-primary);
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: color var(--transition-fast);
}
.po-login:hover { color: var(--color-primary-hover); }

/* 卡片显隐过渡（作用于本组件根节点） */
.popover-enter-active,
.popover-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.popover-enter-from,
.popover-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
</style>
