<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-icon">
          <el-icon :size="22">
            <Lock v-if="mode === 'login'" />
            <UserFilled v-else />
          </el-icon>
        </div>
        <h2>{{ mode === 'login' ? '欢迎回来' : '创建账号' }}</h2>
        <p>{{ mode === 'login' ? '登录你的账号继续使用' : '注册一个新账号开始使用' }}</p>
      </div>

      <form class="auth-form" @submit.prevent="handleSubmit">
        <div class="field">
          <label class="field-label">用户名</label>
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" size="large" />
        </div>
        <div class="field">
          <label class="field-label">密码</label>
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="mode === 'login' ? '请输入密码' : '至少6位密码'"
            :prefix-icon="Lock"
            show-password
            size="large"
            @keyup.enter="handleSubmit"
          />
        </div>
        <div v-if="mode === 'register'" class="field">
          <label class="field-label">确认密码</label>
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="再次输入密码"
            :prefix-icon="Lock"
            show-password
            size="large"
            @keyup.enter="handleSubmit"
          />
        </div>
        <button class="btn btn-primary submit-btn" type="submit" :disabled="loading">
          <el-icon v-if="loading" :size="18" class="is-loading"><Loading /></el-icon>
          {{ loading ? (mode === 'login' ? '登录中...' : '注册中...') : (mode === 'login' ? '登录' : '注册') }}
        </button>
      </form>

      <p class="auth-footer">
        <template v-if="mode === 'login'">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </template>
        <template v-else>
          已有账号？<router-link to="/login">立即登录</router-link>
        </template>
      </p>
    </div>
  </div>
</template>

<script setup>
// 认证表单 - 登录 / 注册共用的卡片式表单
// 通过 mode prop 区分两种模式，共享表单布局、校验逻辑与视觉样式

import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { getErrMsg } from '../utils/error'

const props = defineProps({
  // 'login' 或 'register'
  mode: { type: String, default: 'login' }
})

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
// 加载状态：控制提交按钮的 loading 与禁用
const loading = ref(false)

// 表单数据：确认密码仅注册模式使用
const form = reactive({ username: '', password: '', confirmPassword: '' })

// 处理提交（登录或注册，按 mode 区分）
async function handleSubmit() {
  // 必填校验
  if (!form.username || !form.password || (props.mode === 'register' && !form.confirmPassword)) {
    ElMessage.warning('请填写所有字段')
    return
  }
  // 注册专属校验：密码长度与两次一致性
  if (props.mode === 'register' && form.password.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }
  if (props.mode === 'register' && form.password !== form.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }

  loading.value = true
  try {
    if (props.mode === 'login') {
      // 登录成功：跳回原页面（若有 redirect 参数），否则回首页
      await auth.login(form.username, form.password)
      ElMessage.success('登录成功')
      const redirect = typeof route.query.redirect === 'string' && route.query.redirect
        ? route.query.redirect
        : '/'
      router.push(redirect)
    } else {
      // 注册成功：跳转登录页
      await auth.register(form.username, form.password)
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    }
  } catch (e) {
    ElMessage.error(getErrMsg(e, props.mode === 'login' ? '登录失败' : '注册失败'))
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

/* 全宽主按钮：布局由 .btn 原语承担，这里只控制宽度 */
.submit-btn {
  width: 100%;
  margin-top: 6px;
}

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
