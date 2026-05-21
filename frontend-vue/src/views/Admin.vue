<template>
  <div class="admin-page">
    <div class="page-top">
      <div>
        <h2 class="page-title">后台管理</h2>
        <p class="page-desc">管理图书馆的所有图书信息</p>
      </div>
      <button class="add-btn" @click="openDialog(null)">
        <el-icon :size="16"><Plus /></el-icon>
        添加图书
      </button>
    </div>

    <div class="table-container">
      <el-table :data="books" v-loading="loading" style="width: 100%" table-layout="auto">
        <el-table-column prop="id" label="ID" width="64" align="center" />
        <el-table-column prop="title" label="书名" min-width="180">
          <template #default="{ row }">
            <span class="book-title-cell">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" min-width="120" />
        <el-table-column prop="isbn" label="ISBN" width="140">
          <template #default="{ row }">
            <span class="isbn-text">{{ row.isbn || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <span class="category-badge" v-if="row.category">{{ row.category }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="库存" width="140" align="center">
          <template #default="{ row }">
            <div class="stock-info">
              <span class="stock-avail">{{ row.availableCopies }}</span>
              <span class="stock-sep">/</span>
              <span class="stock-total">{{ row.totalCopies }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <div class="actions">
              <button class="action-btn edit" @click="openDialog(row)">编辑</button>
              <button class="action-btn delete" @click="handleDelete(row.id)">删除</button>
            </div>
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

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑图书' : '添加图书'"
      width="480px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="书名" prop="title">
              <el-input v-model="form.title" placeholder="请输入书名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="form.author" placeholder="请输入作者" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-input v-model="form.category" placeholder="如：计算机、文学等" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="ISBN">
              <el-input v-model="form.isbn" placeholder="13位ISBN号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总副本" prop="totalCopies">
              <el-input-number v-model="form.totalCopies" :min="1" :max="999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <button class="dialog-btn cancel" @click="dialogVisible = false">取消</button>
        <button class="dialog-btn confirm" :disabled="submitting" @click="handleSubmit">
          <el-icon v-if="submitting" :size="15"><Loading /></el-icon>
          {{ submitting ? '提交中...' : '确认' }}
        </button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { bookAPI } from '../api'

const books = ref([])
const loading = ref(true)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const form = reactive({
  title: '',
  author: '',
  isbn: '',
  category: '',
  totalCopies: 1
})

const rules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }]
}

async function fetchBooks() {
  loading.value = true
  try {
    const res = await bookAPI.getAll(currentPage.value, pageSize.value)
    books.value = res.data.records
    total.value = res.data.total
  } catch (_) {
    ElMessage.error('获取图书失败')
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) { currentPage.value = page; fetchBooks() }
function handleSizeChange(size) { pageSize.value = size; currentPage.value = 1; fetchBooks() }

function openDialog(book) {
  editingId.value = book ? book.id : null
  if (book) {
    Object.assign(form, {
      title: book.title,
      author: book.author,
      isbn: book.isbn || '',
      category: book.category || '',
      totalCopies: book.totalCopies
    })
  } else {
    Object.assign(form, { title: '', author: '', isbn: '', category: '', totalCopies: 1 })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload = { ...form }
    if (editingId.value) {
      await bookAPI.update(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await bookAPI.create(payload)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchBooks()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除此图书吗？', '确认删除', { confirmButtonText: '删除', cancelButtonText: '取消' })
    await bookAPI.delete(id)
    ElMessage.success('删除成功')
    fetchBooks()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(fetchBooks)
</script>

<style scoped>
.admin-page { max-width: 1100px; margin: 0 auto; }

.page-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-title { font-size: 26px; font-weight: 700; letter-spacing: -0.02em; margin-bottom: 4px; }
.page-desc { font-size: 14px; color: var(--color-text-secondary); }

.add-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border: none;
  border-radius: var(--radius-lg);
  background: var(--color-primary);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition);
  white-space: nowrap;
}

.add-btn:hover { background: var(--color-primary-hover); }

.table-container {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  overflow: hidden;
}

.book-title-cell { font-weight: 500; }
.isbn-text { font-family: 'SF Mono', 'Fira Code', monospace; font-size: 12px; color: var(--color-text-secondary); }

.category-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 999px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-size: 12px;
  font-weight: 500;
}

.stock-info { display: inline-flex; align-items: center; gap: 3px; }
.stock-avail { font-weight: 700; color: var(--color-success); font-size: 15px; }
.stock-sep { color: var(--color-text-tertiary); }
.stock-total { color: var(--color-text-secondary); }

.actions { display: flex; gap: 6px; }

.action-btn {
  padding: 4px 12px;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition);
}

.action-btn.edit {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.action-btn.edit:hover { background: var(--color-primary); color: #fff; }

.action-btn.delete {
  background: var(--color-danger-light);
  color: var(--color-danger);
}

.action-btn.delete:hover { background: var(--color-danger); color: #fff; }

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* Dialog buttons */
.dialog-btn {
  padding: 8px 20px;
  border: none;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition);
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.dialog-btn.cancel {
  background: var(--color-border-light);
  color: var(--color-text-secondary);
}

.dialog-btn.cancel:hover { background: var(--color-border); }

.dialog-btn.confirm {
  background: var(--color-primary);
  color: #fff;
}

.dialog-btn.confirm:hover:not(:disabled) { background: var(--color-primary-hover); }
.dialog-btn.confirm:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
