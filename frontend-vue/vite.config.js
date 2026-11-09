// Vite 构建工具配置文件
// Vite 是新一代前端构建工具，具有快速的冷启动和热模块替换(HMR)特性

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
// ESM 环境下使用 fileURLToPath 计算项目内绝对路径，用于配置 '@' 别名
import { fileURLToPath, URL } from 'node:url'

// 导出 Vite 配置（defineConfig 提供 TypeScript 智能提示）
export default defineConfig({
  // 插件列表：使用 Vue 3 单文件组件(SFC)编译插件
  plugins: [vue()],
  // 路径别名：'@' 指向 src 目录，新增文件可使用 '@/xxx' 导入
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  // 开发服务器配置
  server: {
    // 前端开发服务器端口号
    port: 5173,
    // 代理配置：解决开发环境前后端跨域问题
    proxy: {
      // 所有以 /api 开头的请求都会被转发
      '/api': {
        // 转发到后端 Spring Boot 服务（默认端口 5000）
        target: 'http://localhost:5000',
        // 修改请求头中的 Origin 为目标地址
        changeOrigin: true
      }
    }
  }
})
