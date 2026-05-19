<template>
  <div class="records-page">
    <h2>借阅记录</h2>

    <el-table :data="records" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="title" label="图书名称" min-width="180" />
      <el-table-column prop="author" label="作者" min-width="140" />
      <el-table-column prop="username" label="借阅人" min-width="100" v-if="isAdmin" />
      <el-table-column label="借阅日期" width="120">
        <template #default="{ row }">{{ formatDate(row.borrowDate) }}</template>
      </el-table-column>
      <el-table-column label="归还日期" width="120">
        <template #default="{ row }">{{ row.returnDate ? formatDate(row.returnDate) : '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'borrowed' ? 'warning' : 'success'" size="small">
            {{ row.status === 'borrowed' ? '借阅中' : '已归还' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button
            type="success" size="small"
            v-if="row.status === 'borrowed'"
            :loading="returningId === row.id"
            @click="handleReturn(row.id)">
            归还
          </el-button>
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && records.length === 0" description="暂无借阅记录" />
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
const isAdmin = computed(() => auth.user?.role === 'admin')

function formatDate(date) {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

async function fetchRecords() {
  loading.value = true
  try {
    const res = await borrowAPI.getRecords()
    records.value = res.data
  } catch (e) {
    ElMessage.error('获取记录失败')
  } finally {
    loading.value = false
  }
}

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
