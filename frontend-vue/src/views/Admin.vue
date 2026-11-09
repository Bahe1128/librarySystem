<template>
  <div class="admin-page">
    <PageHeader title="后台管理" desc="管理图书馆的所有图书信息">
      <button class="btn btn-primary" @click="openDialog(null)">
        <el-icon :size="16"><Plus /></el-icon>
        添加图书
      </button>
    </PageHeader>

    <TableCard>
      <el-table :data="books" v-loading="loading" style="width: 100%" table-layout="auto">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="title" label="书名" min-width="180">
          <template #default="{ row }">
            <span class="book-title-cell">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" min-width="130" />
        <el-table-column prop="isbn" label="ISBN" width="150">
          <template #default="{ row }">
            <span class="isbn-text">{{ row.isbn || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="110">
          <template #default="{ row }">
            <span class="category-badge" v-if="row.category">{{ row.category }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="库存" width="150" align="center">
          <template #default="{ row }">
            <div class="stock-info">
              <span class="stock-avail">{{ row.availableCopies }}</span>
              <span class="stock-sep">/</span>
              <span class="stock-total">{{ row.totalCopies }}</span>
              <span class="stock-label">可用 / 总计</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <div class="actions">
              <button class="action-btn edit" @click="openDialog(row)">编辑</button>
              <button class="action-btn delete" @click="handleDelete(row.id)">删除</button>
            </div>
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

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑图书' : '添加图书'"
      width="500px"
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
              <el-input v-model="form.category" placeholder="如：计算机、文学" />
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
        <div class="dialog-footer">
          <button class="btn btn-ghost" @click="dialogVisible = false">取消</button>
          <button class="btn btn-primary" :disabled="submitting" @click="handleSubmit">
            <el-icon v-if="submitting" :size="15" class="is-loading"><Loading /></el-icon>
            {{ submitting ? '提交中...' : '确认' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// 管理员面板 - 图书的增删改查管理
// 仅管理员可访问（由路由导航守卫控制）
// 功能：添加/编辑/删除图书，以表格形式展示所有图书信息

import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { bookAPI } from '../api'
import { getErrMsg } from '../utils/error'
import PageHeader from '../components/PageHeader.vue'
import TableCard from '../components/TableCard.vue'

// 图书列表数据
const books = ref([])
// 表格加载状态
const loading = ref(true)
// 总数据条数（用于分页）
const total = ref(0)
// 当前页码和每页条数
const currentPage = ref(1)
const pageSize = ref(10)
// 对话框是否可见（新增/编辑图书的弹窗）
const dialogVisible = ref(false)
// 表单提交状态
const submitting = ref(false)
// 正在编辑的图书 ID（null 表示新增模式，有值表示编辑模式）
const editingId = ref(null)
// 表单组件的引用（用于表单验证）
const formRef = ref(null)

// 图书表单数据模型（使用 reactive 实现与表单的双向绑定）
const form = reactive({
  title: '',
  author: '',
  isbn: '',
  category: '',
  totalCopies: 1
})

// 表单验证规则
const rules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }]
}

// 获取图书列表（分页）
async function fetchBooks() {
  loading.value = true
  try {
    const res = await bookAPI.getAll(currentPage.value, pageSize.value)
    books.value = res.records
    total.value = res.total
  } catch (e) {
    ElMessage.error(getErrMsg(e, '获取图书失败'))
  } finally {
    loading.value = false
  }
}

// 分页处理：页码变化
function handlePageChange(page) { currentPage.value = page; fetchBooks() }
// 分页处理：每页条数变化时重置到第1页
function handleSizeChange(size) { pageSize.value = size; currentPage.value = 1; fetchBooks() }

// 打开新增/编辑对话框
// book 为 null 时是新增模式，传入图书对象时是编辑模式
function openDialog(book) {
  // 设置编辑 ID
  editingId.value = book ? book.id : null
  if (book) {
    // 编辑模式：将现有图书数据填充到表单
    Object.assign(form, {
      title: book.title,
      author: book.author,
      isbn: book.isbn || '',
      category: book.category || '',
      totalCopies: book.totalCopies
    })
  } else {
    // 新增模式：重置表单为初始值
    Object.assign(form, { title: '', author: '', isbn: '', category: '', totalCopies: 1 })
  }
  // 显示对话框
  dialogVisible.value = true
}

// 处理表单提交（新增或更新图书）
async function handleSubmit() {
  // 执行 Element Plus 表单验证
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload = { ...form }
    if (editingId.value) {
      // 有编辑 ID → 执行更新操作
      await bookAPI.update(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      // 无编辑 ID → 执行新增操作
      await bookAPI.create(payload)
      ElMessage.success('添加成功')
    }
    // 关闭对话框并刷新列表
    dialogVisible.value = false
    fetchBooks()
  } catch (e) {
    ElMessage.error(getErrMsg(e, '操作失败'))
  } finally {
    submitting.value = false
  }
}

// 删除图书
async function handleDelete(id) {
  try {
    // 弹出确认对话框
    await ElMessageBox.confirm('确定要删除此图书吗？', '确认删除', { confirmButtonText: '删除', cancelButtonText: '取消' })
    await bookAPI.delete(id)
    ElMessage.success('删除成功')
    // 删除后刷新列表
    fetchBooks()
  } catch (e) {
    // 用户取消操作时不显示错误提示
    if (e !== 'cancel') ElMessage.error(getErrMsg(e, '删除失败'))
  }
}

// 组件挂载后自动加载图书列表
onMounted(fetchBooks)
</script>

<style scoped>
.admin-page { max-width: 1100px; margin: 0 auto; }

.book-title-cell { font-weight: 600; color: var(--color-text); }

.isbn-text {
  font-family: 'SF Mono', 'Fira Code', 'Cascadia Code', monospace;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.text-muted { color: var(--color-text-tertiary); }

.category-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: var(--radius-full);
  background: var(--color-primary-50);
  color: var(--color-primary-700);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: -0.01em;
}

html.dark .category-badge {
  background: rgba(124, 90, 228, 0.12);
  color: var(--color-primary-400);
}

.stock-info { display: inline-flex; align-items: center; gap: 4px; }
.stock-avail { font-weight: 700; color: var(--color-success); font-size: 15px; }
.stock-sep { color: var(--color-text-tertiary); }
.stock-total { color: var(--color-text-secondary); font-weight: 500; }
.stock-label { font-size: 11px; color: var(--color-text-tertiary); margin-left: 6px; }

.actions { display: flex; gap: 6px; }

.action-btn {
  padding: 5px 14px;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition-fast);
  letter-spacing: -0.01em;
}

.action-btn.edit {
  background: var(--color-primary-50);
  color: var(--color-primary-700);
}
.action-btn.edit:hover { background: var(--color-primary-100); }

.action-btn.delete {
  background: var(--color-danger-50);
  color: var(--color-danger-700);
}
.action-btn.delete:hover { background: var(--color-danger-100); }

html.dark .action-btn.edit   { background: rgba(124,90,228,0.12); color: var(--color-primary-400); }
html.dark .action-btn.edit:hover   { background: rgba(124,90,228,0.20); }
html.dark .action-btn.delete { background: rgba(239,68,68,0.12); color: var(--color-danger-400); }
html.dark .action-btn.delete:hover { background: rgba(239,68,68,0.20); }

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

/* Dialog */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
