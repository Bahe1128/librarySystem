<template>
  <div class="accounts-page">
    <PageHeader title="账号管理" desc="管理普通用户的账号信息">
      <button class="btn btn-primary" @click="openDialog(null)">
        <el-icon :size="16"><Plus /></el-icon>
        添加用户
      </button>
    </PageHeader>

    <TableCard>
      <el-table :data="users" v-loading="loading" style="width: 100%" table-layout="auto">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="账号" min-width="120">
          <template #default="{ row }">
            <span class="username-cell">{{ row.username }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" min-width="100">
          <template #default="{ row }">
            <span>{{ row.name || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="idCard" label="身份证号" width="190">
          <template #default="{ row }">
            <span class="idcard-text">{{ row.idCard || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140">
          <template #default="{ row }">
            <span>{{ row.phone || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="190">
          <template #default="{ row }">
            <span class="email-text">{{ row.email || '-' }}</span>
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

    <EmptyState
      v-if="!loading && users.length === 0"
      icon="User"
      title="暂无用户"
      desc="还没有普通用户账号"
    />

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑用户' : '添加用户'"
      width="540px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="账号" prop="username">
              <el-input v-model="form.username" placeholder="请输入账号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="密码" :prop="editingId ? null : 'password'">
              <el-input v-model="form.password" type="password" show-password :placeholder="editingId ? '留空则不修改' : '请输入密码'" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
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
// 账号管理页 - 管理普通用户的账号信息
// 仅管理员可访问（由路由导航守卫控制）
// 功能：查看、添加、编辑、删除普通用户

import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userAPI } from '../api'
import { getErrMsg } from '../utils/error'
import PageHeader from '../components/PageHeader.vue'
import TableCard from '../components/TableCard.vue'
import EmptyState from '../components/EmptyState.vue'

// 用户列表数据
const users = ref([])
// 表格加载状态
const loading = ref(true)
// 对话框是否可见（新增/编辑用户的弹窗）
const dialogVisible = ref(false)
// 表单提交状态
const submitting = ref(false)
// 正在编辑的用户 ID（null 表示新增，有值表示编辑）
const editingId = ref(null)
// 表单组件的引用
const formRef = ref(null)

// 用户表单数据模型
const form = reactive({
  username: '',
  password: '',
  name: '',
  idCard: '',
  phone: '',
  email: ''
})

// 表单验证规则：账号、姓名、密码为必填
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  // 密码验证：新增时必须填写，编辑时留空则不修改
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 获取所有普通用户列表（不分页）
async function fetchUsers() {
  loading.value = true
  try {
    const res = await userAPI.getAll()
    // getAll 返回的是数组，直接赋值
    users.value = res
  } catch (e) {
    ElMessage.error(getErrMsg(e, '获取用户列表失败'))
  } finally {
    loading.value = false
  }
}

// 打开新增/编辑对话框
// user 为 null 时是新增模式，传入用户对象时是编辑模式
function openDialog(user) {
  editingId.value = user ? user.id : null
  if (user) {
    // 编辑模式：填充现有数据（密码留空，表示不修改）
    Object.assign(form, {
      username: user.username,
      password: '',
      name: user.name || '',
      idCard: user.idCard || '',
      phone: user.phone || '',
      email: user.email || ''
    })
  } else {
    // 新增模式：重置所有字段
    Object.assign(form, { username: '', password: '', name: '', idCard: '', phone: '', email: '' })
  }
  dialogVisible.value = true
}

// 处理表单提交（新增或更新用户）
async function handleSubmit() {
  // 执行 Element Plus 表单验证
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload = { ...form }
    if (editingId.value) {
      // 编辑模式：如果用户没有修改密码（密码字段为空），则不发送密码字段
      if (!payload.password) delete payload.password
      await userAPI.update(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      // 新增模式：直接创建用户
      await userAPI.create(payload)
      ElMessage.success('添加成功')
    }
    // 关闭对话框并刷新用户列表
    dialogVisible.value = false
    fetchUsers()
  } catch (e) {
    ElMessage.error(getErrMsg(e, '操作失败'))
  } finally {
    submitting.value = false
  }
}

// 删除用户（会同时删除该用户的借阅记录）
async function handleDelete(id) {
  try {
    // 弹出确认对话框，提醒用户删除的后果
    await ElMessageBox.confirm(
      '确定要删除此用户吗？这将同时删除该用户的所有借阅记录。',
      '确认删除',
      { confirmButtonText: '删除', cancelButtonText: '取消' }
    )
    await userAPI.delete(id)
    ElMessage.success('删除成功')
    fetchUsers()
  } catch (e) {
    // 用户取消操作或删除失败
    if (e !== 'cancel') ElMessage.error(getErrMsg(e, '删除失败'))
  }
}

// 组件挂载后自动加载用户列表
onMounted(fetchUsers)
</script>

<style scoped>
.accounts-page { max-width: 1100px; margin: 0 auto; }

.username-cell { font-weight: 600; color: var(--color-text); }

.idcard-text {
  font-family: 'SF Mono', 'Fira Code', 'Cascadia Code', monospace;
  font-size: 13px;
  color: var(--color-text-secondary);
}
.email-text { color: var(--color-text-secondary); }

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

/* Dialog */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
