<template>
  <div class="books-page">
    <div class="page-top">
      <div>
        <h2 class="page-title">图书列表</h2>
        <p class="page-desc">浏览馆藏图书，搜索并借阅你感兴趣的书籍</p>
      </div>
    </div>

    <div class="search-bar">
      <div class="search-input-wrapper">
        <el-icon :size="18" color="var(--color-text-tertiary)"><Search /></el-icon>
        <input
          v-model="keyword"
          class="search-input"
          placeholder="搜索书名、作者、ISBN 或分类..."
          @keyup.enter="handleSearch"
        />
        <button v-if="keyword" class="search-clear" @click="resetSearch">
          <el-icon :size="14"><Close /></el-icon>
        </button>
      </div>
      <button class="search-btn" @click="handleSearch">搜索</button>
    </div>

    <div class="book-grid" v-loading="loading">
      <div class="book-card" v-for="book in books" :key="book.id">
        <div class="book-cover">
          <el-icon :size="32"><Reading /></el-icon>
        </div>
        <div class="book-body">
          <h3 class="book-title">{{ book.title }}</h3>
          <div class="book-meta">
            <span class="book-author">
              <el-icon :size="13"><User /></el-icon>
              {{ book.author }}
            </span>
            <span class="book-category">
              <el-icon :size="13"><Collection /></el-icon>
              {{ book.category || '未分类' }}
            </span>
          </div>
          <div class="book-detail">
            <span class="book-isbn">ISBN {{ book.isbn || '-' }}</span>
          </div>
          <div class="book-footer">
            <div class="copies">
              <span class="copies-available">{{ book.availableCopies }}</span>
              <span class="copies-sep">/</span>
              <span class="copies-total">{{ book.totalCopies }}</span>
              <span class="copies-label">可用</span>
            </div>
            <button
              class="borrow-btn"
              :class="{ disabled: book.availableCopies <= 0, loading: borrowingId === book.id }"
              :disabled="book.availableCopies <= 0 || borrowingId === book.id"
              @click="handleBorrow(book.id)"
            >
              <el-icon v-if="borrowingId === book.id" :size="16"><Loading /></el-icon>
              {{ book.availableCopies <= 0 ? '已借完' : '借阅' }}
            </button>
          </div>
        </div>
        <button
          v-if="auth.user?.role === 'admin'"
          class="book-delete"
          @click="handleDelete(book.id)"
          title="删除图书"
        >
          <el-icon :size="14"><Delete /></el-icon>
        </button>
      </div>
    </div>

    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <div class="empty-state" v-if="!loading && books.length === 0">
      <el-icon :size="48" color="var(--color-text-tertiary)"><FolderOpened /></el-icon>
      <h3>暂无图书</h3>
      <p>没有找到相关图书数据</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { bookAPI, borrowAPI } from '../api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const books = ref([])
const keyword = ref('')
const loading = ref(true)
const borrowingId = ref(null)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

async function fetchBooks(searchKeyword) {
  loading.value = true
  try {
    const res = searchKeyword
      ? await bookAPI.search(searchKeyword, currentPage.value, pageSize.value)
      : await bookAPI.getAll(currentPage.value, pageSize.value)
    books.value = res.data.records
    total.value = res.data.total
  } catch (_) {
    ElMessage.error('获取图书失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() { currentPage.value = 1; fetchBooks(keyword.value) }
function resetSearch() { keyword.value = ''; currentPage.value = 1; fetchBooks('') }
function handlePageChange(page) { currentPage.value = page; fetchBooks(keyword.value) }
function handleSizeChange(size) { pageSize.value = size; currentPage.value = 1; fetchBooks(keyword.value) }

async function handleBorrow(bookId) {
  if (!auth.isLoggedIn) { ElMessage.warning('请先登录'); return }
  borrowingId.value = bookId
  try {
    await borrowAPI.borrow(bookId)
    ElMessage.success('借阅成功')
    fetchBooks(keyword.value)
  } catch (e) {
    ElMessage.error(e.message || '借阅失败')
  } finally {
    borrowingId.value = null
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除此图书吗？')
    await bookAPI.delete(id)
    ElMessage.success('删除成功')
    fetchBooks(keyword.value)
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => fetchBooks())
</script>

<style scoped>
.books-page { max-width: 1100px; margin: 0 auto; }

.page-top { margin-bottom: 24px; }
.page-title { font-size: 26px; font-weight: 700; letter-spacing: -0.02em; margin-bottom: 4px; }
.page-desc { font-size: 14px; color: var(--color-text-secondary); }

/* Search */
.search-bar { display: flex; gap: 10px; margin-bottom: 32px; }

.search-input-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  transition: all var(--transition);
}

.search-input-wrapper:focus-within {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-light);
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 12px 0;
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
  transition: all var(--transition);
}

.search-clear:hover { background: var(--color-border); color: var(--color-text-secondary); }

.search-btn {
  padding: 0 24px;
  border: none;
  border-radius: var(--radius-lg);
  background: var(--color-primary);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition);
}

.search-btn:hover { background: var(--color-primary-hover); }

/* Book Grid */
.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.book-card {
  display: flex;
  padding: 20px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  position: relative;
  transition: all var(--transition);
}

.book-card:hover {
  box-shadow: var(--shadow-lg);
  border-color: transparent;
  transform: translateY(-2px);
}

.book-cover {
  width: 56px;
  height: 76px;
  border-radius: var(--radius-sm);
  background: var(--color-primary-bg);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: 16px;
}

.book-body { flex: 1; min-width: 0; }

.book-title {
  font-size: 15px;
  font-weight: 600;
  letter-spacing: -0.01em;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-bottom: 6px;
}

.book-meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.book-detail { margin-bottom: 12px; }

.book-isbn {
  font-size: 11px;
  color: var(--color-text-tertiary);
  font-family: 'SF Mono', 'Fira Code', monospace;
}

.book-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.copies { display: flex; align-items: baseline; gap: 2px; }
.copies-available { font-size: 18px; font-weight: 700; color: var(--color-success); }
.copies-sep { font-size: 13px; color: var(--color-text-tertiary); margin: 0 2px; }
.copies-total { font-size: 13px; color: var(--color-text-secondary); }
.copies-label { font-size: 11px; color: var(--color-text-tertiary); margin-left: 4px; }

.borrow-btn {
  padding: 6px 18px;
  border: none;
  border-radius: var(--radius-md);
  background: var(--color-primary);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition);
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.borrow-btn:hover:not(:disabled) { background: var(--color-primary-hover); }
.borrow-btn.disabled { background: var(--color-border-light); color: var(--color-text-tertiary); cursor: not-allowed; }
.borrow-btn.loading { opacity: 0.7; }

.book-delete {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-text-tertiary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition);
  opacity: 0;
}

.book-card:hover .book-delete { opacity: 1; }
.book-delete:hover { background: var(--color-danger-light); color: var(--color-danger); }

/* Empty state */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: var(--color-text-secondary);
}

.empty-state h3 { font-size: 16px; font-weight: 600; margin: 16px 0 6px; color: var(--color-text); }
.empty-state p { font-size: 14px; }
</style>
