// 书架页组合式函数 (composable)
//
// 封装图书书架页的全部状态与交互逻辑：数据加载、分类分组、书脊渲染样式、
// 悬浮详情卡定位、借阅/删除、字号调节、滚动分类高亮与生命周期管理。
//
// 抽离自 Books.vue，行为与视觉效果完全保持不变，便于独立维护与测试。
// 参数 popoverRef：悬浮详情卡组件引用（用于测量卡片尺寸以计算定位坐标）。

import { ref, computed, nextTick, watch, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { bookAPI, borrowAPI } from '../api'
import { useAuthStore } from '../stores/auth'
import { getErrMsg } from '../utils/error'

export function useBookshelf(popoverRef) {
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

  // 书架容器 DOM 引用
  const shelfContainer = ref(null)

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

  // ===== 用户操作 =====
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
      // BookPopover 组件通过 defineExpose 暴露根元素，用于测量尺寸
      const pop = popoverRef.value?.el
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
      books.value = res || []
      // 等书架渲染完成后，用真实行宽重新计算每行数量，修正最右侧截断
      await nextTick()
      computeBooksPerRow()
    } catch (e) {
      ElMessage.error(getErrMsg(e, '获取图书失败'))
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
      ElMessage.error(getErrMsg(e, '借阅失败'))
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
      if (e !== 'cancel') ElMessage.error(getErrMsg(e, '删除失败'))
    }
  }

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

  // 导出供模板使用的状态与方法
  return {
    // 数据
    keyword, fontSize, FONT_MIN, FONT_MAX, loading, borrowingId, activeCategory,
    shelfContainer, activeBook, popoverAbove, popoverStyle,
    // 计算属性
    spineGap, allShelves, shelves, shelfCountMap, totalFiltered,
    // 方法
    changeFontSize, spineStyle, startHover, startHide, cancelHide, togglePopover,
    closePopover, scrollToCategory, handleBorrow, handleDelete
  }
}
