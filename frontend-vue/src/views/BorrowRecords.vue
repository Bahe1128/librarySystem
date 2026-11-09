<template>
  <div class="records-page">
    <PageHeader title="借阅记录" :desc="isAdmin ? '查看用户的借阅和归还记录' : '查看你的借阅和归还记录'" />

    <TableCard>
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
        <el-table-column v-if="isAdmin" prop="username" label="借阅人" min-width="120">
          <template #default="{ row }">
            <span class="user-cell">
              <span class="user-avatar-sm">{{ row.username?.charAt(0)?.toUpperCase() }}</span>
              {{ row.username }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="借阅日期" width="140">
          <template #default="{ row }">{{ formatDate(row.borrowDate) }}</template>
        </el-table-column>
        <el-table-column label="应还日期" width="130">
          <template #default="{ row }">
            <span :class="{ 'overdue-text': row.overdue }">
              {{ formatDate(row.dueDate) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="归还日期" width="130">
          <template #default="{ row }">{{ row.returnDate ? formatDate(row.returnDate) : '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <span class="status-badge" :class="row.overdue ? 'overdue' : row.status">
              <span class="status-dot"></span>
              {{ row.overdue ? '已逾期' : (row.status === 'borrowed' ? '借阅中' : '已归还') }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <button
              v-if="row.status === 'borrowed'"
              class="return-btn"
              :disabled="returningId === row.id"
              @click="handleReturn(row.id)"
            >
              <el-icon v-if="returningId === row.id" :size="14" class="is-loading"><Loading /></el-icon>
              归还
            </button>
            <span v-else class="no-action">-</span>
          </template>
        </el-table-column>
      </el-table>
    </TableCard>

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

    <EmptyState
      v-if="!loading && records.length === 0"
      icon="FolderOpened"
      title="暂无借阅记录"
      desc="去图书列表借阅一本感兴趣的书吧"
    />
  </div>
</template>

<script setup>
// 借阅记录页 - 展示用户的借阅和归还历史
// 普通用户只能看到自己的记录，管理员可以看到所有用户的记录
// 支持分页展示和归还操作

import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { borrowAPI } from '../api'
import { useAuthStore } from '../stores/auth'
import { formatDate } from '../utils/format'
import { getErrMsg } from '../utils/error'
import PageHeader from '../components/PageHeader.vue'
import TableCard from '../components/TableCard.vue'
import EmptyState from '../components/EmptyState.vue'

const auth = useAuthStore()
// 借阅记录列表
const records = ref([])
// 表格加载状态
const loading = ref(true)
// 正在执行归还操作的记录 ID（用于 UI loading 反馈）
const returningId = ref(null)
// 总数据条数
const total = ref(0)
// 当前页码和每页条数
const currentPage = ref(1)
const pageSize = ref(10)

// 计算属性：判断当前用户是否为管理员
// 管理员可以看到所有用户的借阅记录，并在表格中显示"借阅人"列
const isAdmin = computed(() => auth.user?.role === 'admin')

// 获取借阅记录列表（分页）
// 后端会根据当前登录用户的角色自动筛选数据
// 管理员获取全部记录，普通用户只获取自己的记录
async function fetchRecords() {
  loading.value = true
  try {
    const res = await borrowAPI.getRecords(currentPage.value, pageSize.value)
    records.value = res.records
    total.value = res.total
  } catch (e) {
    ElMessage.error(getErrMsg(e, '获取记录失败'))
  } finally {
    loading.value = false
  }
}

// 页码变化
function handlePageChange(page) { currentPage.value = page; fetchRecords() }
// 每页条数变化时重置到第1页
function handleSizeChange(size) { pageSize.value = size; currentPage.value = 1; fetchRecords() }

// 归还图书操作
async function handleReturn(id) {
  // 记录正在归还的记录 ID
  returningId.value = id
  try {
    // 调用归还 API
    await borrowAPI.returnBook(id)
    ElMessage.success('归还成功')
    // 归还成功后刷新列表，更新状态显示
    fetchRecords()
  } catch (e) {
    ElMessage.error(getErrMsg(e, '归还失败'))
  } finally {
    returningId.value = null
  }
}

// 组件挂载后自动加载借阅记录
onMounted(fetchRecords)
</script>

<style scoped>
.records-page { max-width: 1100px; margin: 0 auto; }

/* Book Cell */
.book-cell { display: flex; align-items: center; gap: 12px; }
.book-icon-sm {
  width: 34px;
  height: 34px;
  border-radius: var(--radius-md);
  background: var(--color-primary-50);
  color: var(--color-primary-600);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
html.dark .book-icon-sm {
  background: rgba(124, 90, 228, 0.12);
  color: var(--color-primary-400);
}
.book-name { font-weight: 600; color: var(--color-text); }

/* User Cell */
.user-cell { display: flex; align-items: center; gap: 8px; }
.user-avatar-sm {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary-500), var(--color-primary-600));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.15);
}

/* Status Badge */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 5px 12px;
  border-radius: var(--radius-full);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.status-badge.borrowed {
  background: var(--color-warning-50);
  color: var(--color-warning-700);
}
html.dark .status-badge.borrowed {
  background: rgba(245, 158, 11, 0.14);
  color: var(--color-warning-400);
}

.status-badge.overdue {
  background: var(--color-danger-light, #fee2e2);
  color: var(--color-danger, #dc2626);
}

.status-badge.returned {
  background: var(--color-success-50);
  color: var(--color-success-700);
}
html.dark .status-badge.returned {
  background: rgba(16, 185, 129, 0.12);
  color: var(--color-success-400);
}

.overdue-text {
  color: var(--color-danger, #dc2626);
  font-weight: 600;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

/* Return Button */
.return-btn {
  padding: 6px 16px;
  border: none;
  border-radius: var(--radius-md);
  background: var(--color-primary);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition-fast);
  display: inline-flex;
  align-items: center;
  gap: 5px;
  letter-spacing: -0.01em;
  box-shadow: 0px 1px 2px rgba(107, 70, 212, 0.18);
}

.return-btn:hover:not(:disabled) {
  background: var(--color-primary-hover);
  box-shadow: 0px 2px 4px rgba(107, 70, 212, 0.26);
}
.return-btn:disabled { opacity: 0.55; cursor: not-allowed; }

.no-action { color: var(--color-text-tertiary); font-size: 13px; }

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}
</style>
