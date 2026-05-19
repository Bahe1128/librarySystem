<template>
  <div class="admin-page">
    <el-row justify="space-between" class="page-header">
      <h2>后台管理</h2>
      <el-button type="primary" @click="openDialog(null)">添加图书</el-button>
    </el-row>

    <el-table :data="books" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="书名" min-width="160" />
      <el-table-column prop="author" label="作者" min-width="120" />
      <el-table-column prop="isbn" label="ISBN" width="130" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="totalCopies" label="总副本" width="80" align="center" />
      <el-table-column prop="availableCopies" label="可用副本" width="80" align="center" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openDialog(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑图书' : '添加图书'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="书名" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" />
        </el-form-item>
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="form.isbn" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" />
        </el-form-item>
        <el-form-item label="总副本" prop="totalCopies">
          <el-input-number v-model="form.totalCopies" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认</el-button>
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
    const res = await bookAPI.getAll()
    books.value = res.data
  } catch (e) {
    ElMessage.error('获取图书失败')
  } finally {
    loading.value = false
  }
}

function openDialog(book) {
  editingId.value = book ? book.id : null
  if (book) {
    form.title = book.title
    form.author = book.author
    form.isbn = book.isbn || ''
    form.category = book.category || ''
    form.totalCopies = book.totalCopies
  } else {
    form.title = ''
    form.author = ''
    form.isbn = ''
    form.category = ''
    form.totalCopies = 1
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (editingId.value) {
      await bookAPI.update(editingId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await bookAPI.create({ ...form })
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
    await ElMessageBox.confirm('确定要删除此图书吗？')
    await bookAPI.delete(id)
    ElMessage.success('删除成功')
    fetchBooks()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(fetchBooks)
</script>
