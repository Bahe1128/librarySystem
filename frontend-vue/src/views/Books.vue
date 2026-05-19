<template>
  <div class="books-page">
    <el-row :gutter="16" class="page-header">
      <el-col :span="18">
        <el-input v-model="keyword" placeholder="搜索图书（书名/作者/ISBN/分类）" clearable @keyup.enter="handleSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
          <template #append><el-button @click="handleSearch">搜索</el-button></template>
        </el-input>
      </el-col>
      <el-col :span="6" style="text-align: right">
        <el-button @click="resetSearch" v-if="keyword">清除</el-button>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="6" v-for="book in books" :key="book.id" style="margin-bottom: 16px">
        <el-card shadow="hover">
          <h3>{{ book.title }}</h3>
          <p><strong>作者：</strong>{{ book.author }}</p>
          <p><strong>ISBN：</strong>{{ book.isbn || '-' }}</p>
          <p><strong>分类：</strong>{{ book.category || '-' }}</p>
          <p><strong>总副本：</strong>{{ book.totalCopies }}</p>
          <p><strong>可用副本：</strong>
            <el-tag :type="book.availableCopies > 0 ? 'success' : 'danger'" size="small">
              {{ book.availableCopies }}
            </el-tag>
          </p>
          <div style="margin-top: 12px">
            <el-button
              type="primary" size="small"
              :disabled="book.availableCopies <= 0"
              :loading="borrowingId === book.id"
              @click="handleBorrow(book.id)">
              {{ book.availableCopies <= 0 ? '无可用副本' : '借阅' }}
            </el-button>
            <el-button
              type="danger" size="small"
              v-if="auth.isLoggedIn && auth.user?.role === 'admin'"
              @click="handleDelete(book.id)">
              删除
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && books.length === 0" description="暂无图书数据" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { bookAPI, borrowAPI } from '../api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const books = ref([])
const keyword = ref('')
const loading = ref(true)
const borrowingId = ref(null)

async function fetchBooks(searchKeyword) {
  loading.value = true
  try {
    const res = searchKeyword ? await bookAPI.search(searchKeyword) : await bookAPI.getAll()
    books.value = res.data
  } catch (e) {
    ElMessage.error('获取图书失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  fetchBooks(keyword.value)
}

function resetSearch() {
  keyword.value = ''
  fetchBooks('')
}

async function handleBorrow(bookId) {
  if (!auth.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  borrowingId.value = bookId
  try {
    await borrowAPI.borrow(bookId)
    ElMessage.success('借阅成功！')
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
