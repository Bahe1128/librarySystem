<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-icon">
          <el-icon :size="22"><Lock /></el-icon>
        </div>
        <h2>欢迎回来</h2>
        <p>登录你的账号继续使用</p>
      </div>

      <form class="auth-form" @submit.prevent="handleLogin">
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
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            size="large"
            @keyup.enter="handleLogin"
          />
        </div>
        <button class="submit-btn" type="submit" :disabled="loading">
          <el-icon v-if="loading" :size="18" class="is-loading"><Loading /></el-icon>
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <p class="auth-footer">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
// 登录页 - 用户认证入口
// 用户输入用户名和密码后，调用后端 API 登录，成功后跳转首页

import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
// 加载状态：用于控制登录按钮的 loading 动画和禁用
const loading = ref(false)

// 登录表单数据（使用 reactive 实现双向绑定）
const form = reactive({ username: '', password: '' })

// 处理登录提交
async function handleLogin() {
  // 前端表单验证：确保必填字段不为空
  if (!form.username || !form.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  loading.value = true
  try {
    // 调用 Auth Store 的 login 方法，发起后端登录请求
    // 内部会自动保存 Token 和用户信息到 Store 和 localStorage
    await auth.login(form.username, form.password)
    ElMessage.success('登录成功')
    // 登录成功后跳转到首页
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    // 无论成功或失败，最终都要解除 loading 状态
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
  width: 440px;
  padding: 44px 40px 40px;
  background: var(--color-surface);
  border-radius: var(--radius-2xl);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-xl);
}

.auth-header { text-align: center; margin-bottom: 36px; }

.auth-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-lg);
  background: var(--color-primary-50);
  color: var(--color-primary-600);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 18px;
}

html.dark .auth-icon {
  background: rgba(124, 90, 228, 0.12);
  color: var(--color-primary-400);
}

.auth-header h2 {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.02em;
  margin-bottom: 6px;
  color: var(--color-text);
}

.auth-header p {
  font-size: 15px;
  color: var(--color-text-secondary);
}

.auth-form { display: flex; flex-direction: column; gap: 20px; }

.field { display: flex; flex-direction: column; gap: 6px; }
.field-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  letter-spacing: -0.01em;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  margin-top: 6px;
  border: none;
  border-radius: var(--radius-lg);
  background: var(--color-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition-fast);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  letter-spacing: -0.01em;
  box-shadow: 0px 1px 2px rgba(107, 70, 212, 0.20), 0px 4px 10px rgba(107, 70, 212, 0.10);
}

.submit-btn:hover:not(:disabled) {
  background: var(--color-primary-hover);
  box-shadow: 0px 2px 4px rgba(107, 70, 212, 0.28), 0px 8px 16px rgba(107, 70, 212, 0.14);
}
.submit-btn:disabled { opacity: 0.60; cursor: not-allowed; }

.auth-footer {
  text-align: center;
  margin-top: 26px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.auth-footer a {
  font-weight: 600;
  color: var(--color-primary);
}
.auth-footer a:hover { color: var(--color-primary-hover); }
</style>
