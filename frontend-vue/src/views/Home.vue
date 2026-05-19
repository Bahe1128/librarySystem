<template>
  <div class="home">
    <el-card class="hero-card" :body-style="{ padding: '40px' }">
      <h1>图书管理系统</h1>
      <p>欢迎使用图书管理系统，支持图书的添加、编辑、删除、查询，以及借阅和归还等功能。</p>
      <div class="hero-actions">
        <el-button type="primary" size="large" @click="$router.push('/books')">浏览图书</el-button>
        <el-button size="large" @click="$router.push('/login')" v-if="!auth.isLoggedIn">立即登录</el-button>
      </div>
    </el-card>

    <el-row :gutter="20" class="features">
      <el-col :span="8">
        <el-card class="feature-card">
          <el-icon :size="40" color="#409eff"><Reading /></el-icon>
          <h3>图书管理</h3>
          <p>添加、编辑、删除和查询图书信息</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="feature-card">
          <el-icon :size="40" color="#67c23a"><Tickets /></el-icon>
          <h3>借阅管理</h3>
          <p>借阅和归还图书，查看借阅记录</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="feature-card">
          <el-icon :size="40" color="#e6a23c"><DataAnalysis /></el-icon>
          <h3>数据统计</h3>
          <p>系统统计和借阅趋势分析</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'
const auth = useAuthStore()
</script>

<style scoped>
.home { max-width: 1000px; margin: 0 auto; }
.hero-card {
  text-align: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  margin-bottom: 30px;
}
.hero-card h1 { font-size: 2.5rem; margin-bottom: 15px; }
.hero-card p { font-size: 1.1rem; opacity: 0.9; margin-bottom: 20px; }
.hero-actions { display: flex; gap: 12px; justify-content: center; }
.features { margin-top: 20px; }
.feature-card { text-align: center; padding: 20px; }
.feature-card h3 { margin: 15px 0 10px; color: #303133; }
.feature-card p { color: #909399; font-size: 14px; }
</style>
