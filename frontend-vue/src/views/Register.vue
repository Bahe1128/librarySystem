<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-icon">
          <el-icon :size="24"><UserFilled /></el-icon>
        </div>
        <h2>创建账号</h2>
        <p>注册一个新账号开始使用</p>
      </div>

      <form class="auth-form" @submit.prevent="handleRegister">
        <div class="field">
          <label class="field-label">用户名</label>
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
          />
        </div>
        <div class="field">
          <label class="field-label">密码</label>
          <el-input
            v-model="form.password"
            type="password"
            placeholder="至少6位密码"
            :prefix-icon="Lock"
            show-password
            size="large"
          />
        </div>
        <div class="field">
          <label class="field-label">确认密码</label>
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="再次输入密码"
            :prefix-icon="Lock"
            show-password
            size="large"
            @keyup.enter="handleRegister"
          />
        </div>
        <button class="submit-btn" type="submit" :disabled="loading">
          <el-icon v-if="loading" :size="18"><Loading /></el-icon>
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>

      <p class="auth-footer">
        已有账号？<router-link to="/login">立即登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const loading = ref(false)

const form = reactive({ username: '', password: '', confirmPassword: '' })

async function handleRegister() {
  if (!form.username || !form.password || !form.confirmPassword) {
    ElMessage.warning('请填写所有字段')
    return
  }
  if (form.password.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  loading.value = true
  try {
    await auth.register(form.username, form.password)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    ElMessage.error(e.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 65vh;
  padding: 20px;
}

.auth-card {
  width: 420px;
  padding: 40px;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-lg);
}

.auth-header { text-align: center; margin-bottom: 32px; }

.auth-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  background: var(--color-primary-bg);
  color: var(--color-primary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.auth-header h2 {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
  margin-bottom: 6px;
}

.auth-header p {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.auth-form { display: flex; flex-direction: column; gap: 18px; }

.field { display: flex; flex-direction: column; gap: 6px; }
.field-label { font-size: 13px; font-weight: 600; color: var(--color-text); }

.submit-btn {
  width: 100%;
  padding: 12px;
  margin-top: 4px;
  border: none;
  border-radius: var(--radius-lg);
  background: var(--color-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.submit-btn:hover:not(:disabled) { background: var(--color-primary-hover); }
.submit-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.auth-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 13px;
  color: var(--color-text-secondary);
}
</style>
