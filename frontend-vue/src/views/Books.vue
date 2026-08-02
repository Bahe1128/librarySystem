<template>
  <div class="books-page">
    <div class="page-top">
      <h2 class="page-title">图书书架</h2>
      <p class="page-desc">按分类浏览馆藏图书，鼠标悬浮书脊查看详情并借阅</p>
    </div>

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

      <div v-if="!loading && shelves.length === 0" class="empty-state">
        <div class="empty-icon">
          <el-icon :size="40"><FolderOpened /></el-icon>
        </div>
        <h3>{{ keyword ? '未找到相关图书' : '暂无图书' }}</h3>
        <p>{{ keyword ? '换个关键词试试' : '请先在后台上架图书' }}</p>
      </div>
    </div>

    <!-- 悬浮详情卡（单例，仅展示当前悬浮的书本） -->
    <transition name="popover">
      <div
        v-if="activeBook"
        ref="popoverRef"
        class="book-popover"
        :class="{ above: popoverAbove }"
        :style="popoverStyle"
        @mouseenter="cancelHide"
        @mouseleave="startHide"
      >
        <div class="po-header">
          <span class="po-title">{{ activeBook.title }}</span>
          <span class="po-badge" :class="availClass(activeBook)">{{ statusText(activeBook) }}</span>
        </div>
        <div class="po-body">
          <div class="po-row"><span class="po-label">作者</span><span class="po-value">{{ activeBook.author || '-' }}</span></div>
          <div class="po-row"><span class="po-label">ISBN</span><span class="po-value mono">{{ activeBook.isbn || '-' }}</span></div>
          <div class="po-row"><span class="po-label">分类</span><span class="po-value">{{ activeBook.category || '未分类' }}</span></div>
          <div class="po-row">
            <span class="po-label">馆藏</span>
            <span class="po-value">
              <b class="po-copies" :class="availClass(activeBook)">{{ activeBook.availableCopies }}</b>
              <span class="po-copies-sep">/</span>
              <span>{{ activeBook.totalCopies }} 本可用</span>
            </span>
          </div>
        </div>
        <div class="po-actions">
          <template v-if="auth.isLoggedIn">
            <button
              class="po-btn po-borrow"
              :class="{ disabled: activeBook.availableCopies <= 0, loading: borrowingId === activeBook.id }"
              :disabled="activeBook.availableCopies <= 0 || borrowingId === activeBook.id"
              @click="handleBorrow(activeBook)"
            >
              <el-icon v-if="borrowingId === activeBook.id" class="is-loading"><Loading /></el-icon>
              {{ activeBook.availableCopies <= 0 ? '已借完' : '借阅' }}
            </button>
            <button
              v-if="auth.user?.role === 'admin'"
              class="po-btn po-delete"
              @click="handleDelete(activeBook)"
            >
              <el-icon :size="14"><Delete /></el-icon> 删除
            </button>
          </template>
          <router-link v-else to="/login" class="po-login">
            登录后可借阅 <el-icon :size="12"><ArrowRight /></el-icon>
          </router-link>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
// 图书管理页 - 分类书架展示
// 所有图书按分类分组，每个分类一个书架，书脊排列在木质层板上
// 鼠标悬浮书脊弹出详情卡，支持借阅与删除操作；搜索在客户端即时过滤

import { ref, computed, nextTick, watch, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { bookAPI, borrowAPI } from '../api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()

// ===== 数据状态 =====
// 全部图书（前端内存数据，一次加载后本地过滤/分组）
const books = ref([])
// 搜索关键词（输入即过滤）
const keyword = ref('')
// 书脊字号（可调节，范围 9~20px，持久化到 localStorage）
const FONT_MIN = 9
const FONT_MAX = 20
const fontSize = ref(12)
// 字体缩放系数：以默认 12px 为基准，书本整体尺寸随字号等比缩放
const fontScale = computed(() => fontSize.value / 12)
// 书脊宽度与间距随字号缩放（默认字号时分别为 34px 与 5px）
const spineW = computed(() => Math.round(34 * fontScale.value))
const spineGap = computed(() => Math.round(5 * fontScale.value))
// 加载状态
const loading = ref(true)
// 正在借阅的图书 ID（用于按钮 loading）
const borrowingId = ref(null)
// 每行可容纳的书脊数量（根据容器宽度动态计算）
const booksPerRow = ref(20)
// 当前滚动位置对应的分类（用于分类导航高亮，null 表示顶部「全部」）
const activeCategory = ref(null)

// 书架容器与悬浮卡 DOM 引用
const shelfContainer = ref(null)
const popoverRef = ref(null)

// ===== 悬浮详情卡状态 =====
// 当前悬浮的图书（为 null 时不显示卡片）
const activeBook = ref(null)
// 卡片相对书脊的方位（上方/下方）
const popoverAbove = ref(true)
// 卡片定位样式（fixed 定位）
const popoverStyle = ref({ left: '0px', top: '0px' })

// 显隐控制的定时器（防止快速扫过书脊时卡片闪烁）
let hoverTimer = null
let hideTimer = null

// ===== 常量配置 =====
// 分类展示顺序（其余新分类排最后）
const CATEGORY_ORDER = ['计算机', '文学', '科幻', '科学', '历史', '哲学', '经济', '心理学', '艺术', '教育', '医学', '法律', '政治', '社会学', '语言学']
// 分类色相映射（用于生成不同颜色的书脊）
const CATEGORY_HUES = {
  '计算机': 220, '文学': 330, '科幻': 195, '科学': 160, '历史': 40,
  '哲学': 280, '经济': 45, '心理学': 300, '艺术': 0, '教育': 100,
  '医学': 350, '法律': 215, '政治': 15, '社会学': 140, '语言学': 260
}

// ===== 计算属性 =====
// 按关键词过滤全部图书（标题/作者/ISBN/分类，忽略大小写）
const searchResults = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return books.value
  return books.value.filter(b =>
    (b.title || '').toLowerCase().includes(kw) ||
    (b.author || '').toLowerCase().includes(kw) ||
    (b.isbn || '').toLowerCase().includes(kw) ||
    (b.category || '').toLowerCase().includes(kw)
  )
})

// 全部书架（用于导航 chips 展示所有分类）
const allShelves = computed(() => buildShelves(books.value))
// 过滤后的书架（用于页面渲染）
const shelves = computed(() => buildShelves(searchResults.value))

// 过滤后各分类数量（chips 上的计数，搜索时同步更新）
const shelfCountMap = computed(() => {
  const map = {}
  for (const s of shelves.value) map[s.name] = s.count
  return map
})

// 总匹配数（「全部」chip 的计数）
const totalFiltered = computed(() => searchResults.value.length)

// 调整书脊字号并持久化
function changeFontSize(delta) {
  const next = Math.min(FONT_MAX, Math.max(FONT_MIN, fontSize.value + delta))
  fontSize.value = next
  localStorage.setItem('library-spine-font', String(next))
}

// ===== 书架构建 =====
// 将图书列表按分类分组，每分类按每行容量切成多层书架
function buildShelves(list) {
  const map = new Map()
  for (const b of list) {
    const c = b.category || '未分类'
    if (!map.has(c)) map.set(c, [])
    map.get(c).push(b)
  }
  return [...map.keys()]
    .sort((a, b) => {
      const ia = CATEGORY_ORDER.indexOf(a)
      const ib = CATEGORY_ORDER.indexOf(b)
      const ra = ia === -1 ? 99 : ia
      const rb = ib === -1 ? 99 : ib
      return ra - rb || map.get(b).length - map.get(a).length
    })
    .map(name => ({ name, count: map.get(name).length, rows: chunkRows(map.get(name)) }))
}

// 将一本书架的书切成多行
function chunkRows(list) {
  const per = Math.max(1, booksPerRow.value)
  const rows = []
  for (let i = 0; i < list.length; i += per) rows.push(list.slice(i, i + per))
  return rows
}

// ===== 书脊样式 =====
// 按分类色相生成书脊颜色，单本书在饱和度/亮度上微调以模拟真实书籍色差
// 书脊的宽度、高度、字号全部随字体缩放系数同步变化，保证比例协调
function spineStyle(book) {
  const hue = CATEGORY_HUES[book.category] ?? ((book.id * 47) % 360)
  const sat = 55 + (book.id % 4) * 6    // 55% ~ 73%
  const lig = 42 + (book.id % 5) * 4    // 42% ~ 58%
  const len = Math.min((book.title || '').length, 9)
  const baseHeight = 126 + len * 3 + (book.id % 3) * 2  // 书脊高度随书名长度变化，如真实书籍
  return {
    background: `hsl(${hue}, ${sat}%, ${lig}%)`,
    width: `${spineW.value}px`,
    height: `${Math.round(baseHeight * fontScale.value)}px`
  }
}

// ===== 悬浮详情卡逻辑 =====
// 鼠标移入书脊：延迟 200ms 显示卡片（给用户时间把鼠标移出，避免扫过时闪烁）
function startHover(book, event) {
  cancelHide()
  clearTimeout(hoverTimer)
  if (activeBook.value?.id === book.id) return
  hoverTimer = setTimeout(() => showPopover(book, event.currentTarget), 200)
}

// 鼠标移出书脊：延迟 200ms 隐藏（给时间移入卡片本身）
function startHide() {
  clearTimeout(hoverTimer)
  if (!activeBook.value) return
  clearTimeout(hideTimer)
  hideTimer = setTimeout(() => { activeBook.value = null }, 200)
}

// 取消隐藏（鼠标进入卡片时调用）
function cancelHide() { clearTimeout(hideTimer) }

// 点击书脊：切换卡片显示/隐藏（同时支持触屏设备）
function togglePopover(book, event) {
  if (activeBook.value?.id === book.id) {
    activeBook.value = null
    return
  }
  cancelHide()
  clearTimeout(hoverTimer)
  showPopover(book, event.currentTarget)
}

// 显示卡片：根据书脊位置计算定位，自动避让视口上下边缘
function showPopover(book, el) {
  const rect = el.getBoundingClientRect()
  activeBook.value = book
  nextTick(() => {
    const pop = popoverRef.value
    if (!pop || activeBook.value?.id !== book.id) return
    const popW = pop.offsetWidth
    const popH = pop.offsetHeight
    const spaceAbove = rect.top
    const spaceBelow = window.innerHeight - rect.bottom
    const above = spaceAbove >= spaceBelow
    let left = rect.left + rect.width / 2 - popW / 2
    left = Math.max(8, Math.min(left, window.innerWidth - popW - 8))
    const top = above
      ? Math.max(8, rect.top - popH - 12)
      : Math.min(window.innerHeight - popH - 8, rect.bottom + 12)
    popoverAbove.value = above
    popoverStyle.value = { left: `${left}px`, top: `${top}px` }
  })
}

function closePopover() { activeBook.value = null }

// ===== 尺寸计算与滚动监听 =====
// 根据实际可用宽度计算每行书脊数量
// 优先测量已渲染书架行的内容宽度（扣除左右内边距），避免最右侧书脊被截断
function computeBooksPerRow() {
  const el = shelfContainer.value
  if (!el) return
  const row = el.querySelector('.shelf-row')
  // row.clientWidth 含左右 2px 内边距；数据未加载时用容器宽度扣除书架内边距(22px*2)与边框(1px*2)估算
  const width = row ? row.clientWidth - 4 : el.clientWidth - 46
  booksPerRow.value = Math.max(1, Math.floor((width + spineGap.value) / (spineW.value + spineGap.value)))
}

// 滚动时：关闭悬浮卡，并更新当前高亮分类
function onScroll() {
  closePopover()
  const container = shelfContainer.value
  if (!container) return
  const offset = 120 // sticky 导航高度补偿
  const sections = container.querySelectorAll('[data-category]')
  let current = null
  for (const sec of sections) {
    if (sec.getBoundingClientRect().top <= offset) current = sec.getAttribute('data-category')
    else break
  }
  activeCategory.value = current
}

// 点击分类 chip：平滑滚动到对应书架（null 表示回到顶部）
function scrollToCategory(name) {
  if (name === null) {
    window.scrollTo({ top: 0, behavior: 'smooth' })
    return
  }
  const el = shelfContainer.value?.querySelector(`[data-category="${name}"]`)
  el?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

// ===== 数据加载 =====
async function fetchBooks() {
  loading.value = true
  try {
    const res = await bookAPI.getAllFlat()
    books.value = res.data || []
    // 等书架渲染完成后，用真实行宽重新计算每行数量，修正最右侧截断
    await nextTick()
    computeBooksPerRow()
  } catch (_) {
    ElMessage.error('获取图书失败')
  } finally {
    loading.value = false
  }
}

// ===== 借阅 =====
async function handleBorrow(book) {
  if (!auth.isLoggedIn) { ElMessage.warning('请先登录'); return }
  if (book.availableCopies <= 0) return
  borrowingId.value = book.id
  try {
    await borrowAPI.borrow(book.id)
    book.availableCopies -= 1 // 本地更新库存，无需重新请求
    ElMessage.success('借阅成功')
  } catch (e) {
    ElMessage.error(e.message || '借阅失败')
  } finally {
    borrowingId.value = null
  }
}

// ===== 删除（仅管理员）=====
async function handleDelete(book) {
  try {
    await ElMessageBox.confirm(`确定要删除《${book.title}》吗？`, '删除图书', { type: 'warning' })
    await bookAPI.delete(book.id)
    ElMessage.success('删除成功')
    const idx = books.value.findIndex(b => b.id === book.id)
    if (idx > -1) books.value.splice(idx, 1) // 本地移除，无需重新请求
    closePopover()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

// ===== 展示辅助 =====
function statusText(book) { return book.availableCopies > 0 ? '可借' : '已借完' }
function availClass(book) { return book.availableCopies > 0 ? 'ok' : 'off' }

// ===== 生命周期 =====
let ro = null
onMounted(() => {
  // 读取保存的书脊字号设置
  const savedFont = parseInt(localStorage.getItem('library-spine-font'), 10)
  if (!Number.isNaN(savedFont)) fontSize.value = Math.min(FONT_MAX, Math.max(FONT_MIN, savedFont))

  computeBooksPerRow()
  ro = new ResizeObserver(computeBooksPerRow)
  if (shelfContainer.value) ro.observe(shelfContainer.value)
  window.addEventListener('scroll', onScroll, { passive: true })
  window.addEventListener('resize', closePopover)
  fetchBooks()
})

onBeforeUnmount(() => {
  ro?.disconnect()
  window.removeEventListener('scroll', onScroll)
  window.removeEventListener('resize', closePopover)
  clearTimeout(hoverTimer)
  clearTimeout(hideTimer)
})

// 关键词变化时关闭悬浮卡（避免卡片指向已隐藏的书脊）
watch(keyword, () => closePopover())
// 字号变化时：书脊尺寸变化，需重新计算每行数量，并关闭悬浮卡
watch(fontSize, () => { computeBooksPerRow(); closePopover() })
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

/* ===== Page Header ===== */
.page-top { margin-bottom: 28px; }
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

/* ===== Empty State ===== */
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
.empty-state h3 { font-size: 17px; font-weight: 600; margin-bottom: 6px; color: var(--color-text); }
.empty-state p { font-size: 14px; }

/* ===== Popover Card ===== */
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

.po-body {
  margin-bottom: 2px;
}

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

/* 卡片显隐过渡 */
.popover-enter-active,
.popover-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.popover-enter-from,
.popover-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

@media (max-width: 768px) {
  .bookcase { padding: 16px 14px 12px; }
}
</style>
