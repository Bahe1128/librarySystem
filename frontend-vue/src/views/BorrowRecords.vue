<template>
  <div class="records-page">
    <div class="page-top">
      <div>
        <h2 class="page-title">借阅记录</h2>
        <p class="page-desc">{{ isAdmin ? '查看用户的借阅和归还记录' : '查看你的借阅和归还记录' }}</p>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="records" v-loading="loading" style="width: 100%" table-layout="auto">
        <el-table-column prop="title" label="图书名称" min-width="200">
          <template #default="{ row }">
            <div class="book-cell">
              <div class="book-icon-sm">
                <el-icon :size="14"><Reading /></el-icon>
              </div>
              <span class="book-name">{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" min-width="140" />
        <el-table-column v-if="isAdmin" prop="username" label="借阅人" min-width="110">
          <template #default="{ row }">
            <span class="user-cell">
              <span class="user-avatar-sm">{{ row.username?.charAt(0)?.toUpperCase() }}</span>
              {{ row.username }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="借阅日期" width="130">
          <template #default="{ row }">{{ formatDate(row.borrowDate) }}</template>
        </el-table-column>
        <el-table-column label="归还日期" width="130">
          <template #default="{ row }">{{ row.returnDate ? formatDate(row.returnDate) : '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <span class="status-badge" :class="row.status">
              <span class="status-dot"></span>
              {{ row.status === 'borrowed' ? '借阅中' : '已归还' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <button
              v-if="row.status === 'borrowed'"
              class="return-btn"
              :disabled="returningId === row.id"
              @click="handleReturn(row.id)"
            >
              <el-icon v-if="returningId === row.id" :size="14"><Loading /></el-icon>
              归还
            </button>
            <span v-else class="no-action">-</span>
          </template>
        </el-table-column>
      </el-table>
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

    <div class="empty-state" v-if="!loading && records.length === 0">
      <el-icon :size="48" color="var(--color-text-tertiary)"><FolderOpened /></el-icon>
      <h3>暂无借阅记录</h3>
      <p>去图书列表借阅一本感兴趣的书吧</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { borrowAPI } from '../api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const records = ref([])
const loading = ref(true)
const returningId = ref(null)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const isAdmin = computed(() => auth.user?.role === 'admin')

function formatDate(date) {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

async function fetchRecords() {
  loading.value = true
  try {
    const res = await borrowAPI.getRecords(currentPage.value, pageSize.value)
    records.value = res.data.records
    total.value = res.data.total
  } catch (_) {
    ElMessage.error('获取记录失败')
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) { currentPage.value = page; fetchRecords() }
function handleSizeChange(size) { pageSize.value = size; currentPage.value = 1; fetchRecords() }

async function handleReturn(id) {
  returningId.value = id
  try {
    await borrowAPI.returnBook(id)
    ElMessage.success('归还成功')
    fetchRecords()
  } catch (e) {
    ElMessage.error(e.message || '归还失败')
  } finally {
    returningId.value = null
  }
}

onMounted(fetchRecords)
</script>

<style scoped>
.records-page { max-width: 1100px; margin: 0 auto; }

.page-top { margin-bottom: 24px; }
.page-title { font-size: 26px; font-weight: 700; letter-spacing: -0.02em; margin-bottom: 4px; }
.page-desc { font-size: 14px; color: var(--color-text-secondary); }

.table-container {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  overflow: hidden;
}

.book-cell { display: flex; align-items: center; gap: 10px; }
.book-icon-sm {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  background: var(--color-primary-bg);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.book-name { font-weight: 500; }

.user-cell { display: flex; align-items: center; gap: 8px; }
.user-avatar-sm {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: var(--color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.borrowed {
  background: var(--color-warning-light);
  color: var(--color-warning);
}

.status-badge.returned {
  background: var(--color-success-light);
  color: var(--color-success);
}

.status-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: currentColor;
}

.return-btn {
  padding: 5px 14px;
  border: none;
  border-radius: var(--radius-sm);
  background: var(--color-primary);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition);
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.return-btn:hover:not(:disabled) { background: var(--color-primary-hover); }
.return-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.no-action { color: var(--color-text-tertiary); font-size: 13px; }

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
