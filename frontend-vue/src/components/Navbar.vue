<template>
  <el-menu mode="horizontal" :ellipsis="false" class="navbar">
    <el-menu-item index="0" class="brand">
      <router-link to="/"><el-icon><Reading /></el-icon> 图书管理系统</router-link>
    </el-menu-item>
    <div class="flex-grow" />
    <el-menu-item index="1"><router-link to="/">首页</router-link></el-menu-item>
    <el-menu-item index="2"><router-link to="/books">图书列表</router-link></el-menu-item>
    <el-menu-item index="3" v-if="auth.isLoggedIn"><router-link to="/borrow-records">借阅记录</router-link></el-menu-item>
    <el-menu-item index="4" v-if="auth.user?.role === 'admin'"><router-link to="/admin">后台管理</router-link></el-menu-item>
    <el-sub-menu index="5" v-if="auth.isLoggedIn">
      <template #title>
        <el-icon><User /></el-icon> {{ auth.user?.username }}
      </template>
      <el-menu-item @click="auth.logout()">退出登录</el-menu-item>
    </el-sub-menu>
    <template v-else>
      <el-menu-item index="6"><router-link to="/login">登录</router-link></el-menu-item>
      <el-menu-item index="7"><router-link to="/register">注册</router-link></el-menu-item>
    </template>
  </el-menu>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'
const auth = useAuthStore()
</script>

<style scoped>
.navbar {
  padding: 0 20px;
  border-bottom: 1px solid #e4e7ed;
}
.brand a {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
  text-decoration: none;
}
.flex-grow {
  flex-grow: 1;
}
.el-menu-item a {
  text-decoration: none;
  color: inherit;
}
</style>
