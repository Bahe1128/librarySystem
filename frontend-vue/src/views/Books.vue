<template>
  <div class="books-page">
    <PageHeader
      title="图书书架"
      desc="按分类浏览馆藏图书，鼠标悬浮书脊查看详情并借阅"
    />

    <!-- 搜索栏：输入即在前端内存数据中即时过滤；右侧为书脊字号调节 -->
    <div class="search-bar">
      <div class="search-input-wrapper">
        <el-icon :size="18" color="var(--color-text-tertiary)"><Search /></el-icon>
        <input
          v-model="keyword"
          class="search-input"
          placeholder="搜索书名、作者、ISBN 或分类..."
        />
        <button v-if="keyword" class="search-clear" @click="keyword = ''" title="清空搜索">
          <el-icon :size="14"><Close /></el-icon>
        </button>
      </div>
      <div class="font-control" title="调整书脊文字大小（自动保存）">
        <span class="font-label">书脊字号</span>
        <button class="font-btn" :disabled="fontSize <= FONT_MIN" @click="changeFontSize(-1)">
          <el-icon :size="13"><Minus /></el-icon>
        </button>
        <span class="font-value">{{ fontSize }}px</span>
        <button class="font-btn" :disabled="fontSize >= FONT_MAX" @click="changeFontSize(1)">
          <el-icon :size="13"><Plus /></el-icon>
        </button>
      </div>
    </div>

    <!-- 分类导航：sticky 吸顶，点击平滑滚动到对应书架 -->
    <nav class="category-nav">
      <button class="chip" :class="{ active: activeCategory === null }" @click="scrollToCategory(null)">
        全部 <span class="chip-count">{{ totalFiltered }}</span>
      </button>
      <button
        v-for="s in allShelves"
        :key="s.name"
        class="chip"
        :class="{ active: activeCategory === s.name, disabled: shelfCountMap[s.name] === 0 }"
        :disabled="shelfCountMap[s.name] === 0"
        @click="scrollToCategory(s.name)"
      >
        {{ s.name }} <span class="chip-count">{{ shelfCountMap[s.name] || 0 }}</span>
      </button>
    </nav>

    <!-- 书架区域 -->
    <div ref="shelfContainer" v-loading="loading" class="shelves">
      <section
        v-for="shelf in shelves"
        :key="shelf.name"
        class="bookcase"
        :data-category="shelf.name"
      >
        <div class="bookcase-header">
          <div class="bookcase-title-row">
            <h3>{{ shelf.name }}</h3>
            <span class="bookcase-count">{{ shelf.count }} 本</span>
          </div>
        </div>
        <div class="bookcase-body">
          <template v-for="(row, ri) in shelf.rows" :key="ri">
            <div class="shelf-row" :style="{ gap: spineGap + 'px' }">
              <div
                v-for="book in row"
                :key="book.id"
                class="book-spine"
                :style="spineStyle(book)"
                @mouseenter="startHover(book, $event)"
                @mouseleave="startHide"
                @click="togglePopover(book, $event)"
              >
                <span class="spine-status" :class="{ off: book.availableCopies <= 0 }"></span>
                <span class="spine-title" :style="{ fontSize: fontSize + 'px' }">{{ book.title }}</span>
              </div>
            </div>
            <div class="shelf-board"></div>
          </template>
        </div>
      </section>

      <EmptyState
        v-if="!loading && shelves.length === 0"
        icon="FolderOpened"
        :title="keyword ? '未找到相关图书' : '暂无图书'"
        :desc="keyword ? '换个关键词试试' : '请先在后台上架图书'"
      />
    </div>

    <!-- 悬浮详情卡（单例，仅展示当前悬浮的书本） -->
    <transition name="popover">
      <BookPopover
        v-if="activeBook"
        ref="popoverRef"
        :book="activeBook"
        :above="popoverAbove"
        :style="popoverStyle"
        :borrowing-id="borrowingId"
        :is-logged-in="auth.isLoggedIn"
        :is-admin="auth.user?.role === 'admin'"
        @borrow="handleBorrow"
        @delete="handleDelete"
        @mouseenter="cancelHide"
        @mouseleave="startHide"
      />
    </transition>
  </div>
</template>

<script setup>
// 图书管理页 - 分类书架展示
// 所有图书按分类分组，每个分类一个书架，书脊排列在木质层板上
// 鼠标悬浮书脊弹出详情卡，支持借阅与删除操作；搜索在客户端即时过滤
// 全部逻辑封装在 useBookshelf 组合式函数中，悬浮卡为独立组件 BookPopover

import { ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useBookshelf } from '../composables/useBookshelf'
import PageHeader from '../components/PageHeader.vue'
import EmptyState from '../components/EmptyState.vue'
import BookPopover from '../components/BookPopover.vue'

const auth = useAuthStore()
// 悬浮详情卡组件引用（传给 useBookshelf 用于测量卡片尺寸）
const popoverRef = ref(null)

// 书架全部状态与交互逻辑（数据、分组、定位、借阅删除、生命周期）
const {
  // 数据状态
  keyword, fontSize, FONT_MIN, FONT_MAX, loading, borrowingId, activeCategory,
  shelfContainer, activeBook, popoverAbove, popoverStyle,
  // 计算属性
  spineGap, allShelves, shelves, shelfCountMap, totalFiltered,
  // 方法
  changeFontSize, spineStyle, startHover, startHide, cancelHide, togglePopover,
  scrollToCategory, handleBorrow, handleDelete
} = useBookshelf(popoverRef)
</script>

<style scoped>
/* 书架主题变量：日间为浅木色，暗色模式下覆盖为深木色 */
.books-page {
  --shelf-back: #faf7f2;
  --shelf-wood-top: #d4af7a;
  --shelf-wood-bottom: #a67c46;
}
html.dark .books-page {
  --shelf-back: #13161d;
  --shelf-wood-top: #5a3d1e;
  --shelf-wood-bottom: #3d2810;
}

.books-page { max-width: 1200px; margin: 0 auto; }

/* ===== Search Bar ===== */
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.search-input-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  transition: all var(--transition-fast);
}
.search-input-wrapper:focus-within {
  border-color: var(--color-primary-300);
  box-shadow: var(--focus-ring);
}
.search-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 11px 0;
  font-size: 14px;
  font-family: inherit;
  color: var(--color-text);
  background: transparent;
}
.search-input::placeholder { color: var(--color-text-tertiary); }
.search-clear {
  border: none;
  background: var(--color-border-light);
  color: var(--color-text-tertiary);
  cursor: pointer;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}
.search-clear:hover { background: var(--color-border); color: var(--color-text-secondary); }

/* 书脊字号调节控件 */
.font-control {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 14px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  flex-shrink: 0;
}
.font-label { font-size: 12px; color: var(--color-text-tertiary); white-space: nowrap; font-weight: 500; }
.font-btn {
  border: none;
  background: var(--color-border-light);
  color: var(--color-text-secondary);
  width: 26px;
  height: 26px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
}
.font-btn:hover:not(:disabled) { background: var(--color-border); color: var(--color-text); }
.font-btn:disabled { opacity: 0.35; cursor: not-allowed; }
.font-value {
  font-size: 13px;
  font-weight: 600;
  min-width: 34px;
  text-align: center;
  color: var(--color-text);
}

/* ===== Category Nav ===== */
.category-nav {
  position: sticky;
  top: 68px;
  z-index: 30;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px 0 18px;
  margin-bottom: 28px;
  background: color-mix(in srgb, var(--color-bg) 90%, transparent);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 15px;
  border-radius: var(--radius-full);
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-text-secondary);
  font-size: 13px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition-fast);
  letter-spacing: -0.01em;
}
.chip-count {
  font-size: 11px;
  font-weight: 600;
  line-height: 1;
  color: var(--color-text-tertiary);
  background: var(--color-border-light);
  border-radius: var(--radius-full);
  padding: 3px 7px;
  min-width: 20px;
  text-align: center;
}
.chip:hover:not(:disabled) {
  border-color: var(--color-primary-300);
  color: var(--color-primary);
  background: var(--color-primary-25);
}
.chip.active {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #fff;
  box-shadow: 0px 1px 3px rgba(107, 70, 212, 0.25);
}
.chip.active .chip-count {
  background: rgba(255, 255, 255, 0.20);
  color: #fff;
}
.chip:disabled { opacity: 0.40; cursor: not-allowed; }

/* ===== Shelves ===== */
.shelves { min-height: 300px; }

.bookcase {
  background: var(--shelf-back);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  padding: 24px 24px 18px;
  margin-bottom: 40px;
  scroll-margin-top: 100px;
}

.bookcase-header {
  margin-bottom: 18px;
}
.bookcase-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.bookcase-title-row h3 {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: -0.01em;
  color: var(--color-text);
}
.bookcase-count {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-tertiary);
  background: var(--color-border);
  padding: 3px 10px;
  border-radius: var(--radius-full);
}

.shelf-row {
  display: flex;
  align-items: flex-end;
  padding: 0 2px;
}

/* ===== Book Spine ===== */
.book-spine {
  position: relative;
  border-radius: 3px 3px 4px 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.25),
    inset -3px 0 6px rgba(0, 0, 0, 0.12),
    0 2px 4px rgba(0, 0, 0, 0.15);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}
.book-spine:hover {
  transform: translateY(-5px);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.25),
    inset -3px 0 6px rgba(0, 0, 0, 0.12),
    0 10px 20px rgba(0, 0, 0, 0.22);
}

.spine-title {
  writing-mode: vertical-rl;
  text-orientation: mixed;
  font-size: 10px;
  line-height: 1.25;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.35);
  letter-spacing: 0.5px;
  max-height: 100%;
  overflow: hidden;
  padding: 12px 1px 8px;
  user-select: none;
}

.spine-status {
  position: absolute;
  top: 5px;
  left: 50%;
  transform: translateX(-50%);
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #4ade80;
  box-shadow: 0 0 0 1.5px rgba(0, 0, 0, 0.20);
  z-index: 1;
}
.spine-status.off { background: #94a3b8; }

/* ===== Shelf Board (wood) ===== */
.shelf-board {
  height: 14px;
  margin: 0 2px 26px;
  background: linear-gradient(180deg, var(--shelf-wood-top), var(--shelf-wood-bottom));
  border-radius: 0 0 6px 6px;
  box-shadow:
    inset 0 2px 3px rgba(255, 255, 255, 0.20),
    0 4px 8px rgba(0, 0, 0, 0.15);
}

@media (max-width: 768px) {
  .bookcase { padding: 16px 14px 12px; }
}
</style>
