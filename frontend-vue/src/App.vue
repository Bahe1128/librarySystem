<template>
  <div id="app-wrapper">
    <Navbar />
    <main class="main-container">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
// 根组件 - 应用的布局框架
// 包含导航栏(Navbar)和主内容区域(router-view)，所有页面都在此框架内渲染

// 引入导航栏组件
import Navbar from './components/Navbar.vue'
</script>

<style>
.main-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 40px 32px 80px;
  min-height: calc(100vh - 68px);
}

.page-enter-active,
.page-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 768px) {
  .main-container {
    padding: 24px 16px 64px;
  }
}
</style>
