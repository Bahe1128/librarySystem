// 主题 Store - 管理日间/夜间模式
// 偏好保存在 localStorage 中，未设置时跟随系统主题
// 通过给 <html> 添加 dark 类来切换暗色样式

import { defineStore } from 'pinia'
import { ref } from 'vue'

const STORAGE_KEY = 'library-theme'

export const useThemeStore = defineStore('theme', () => {
  // 当前是否为夜间模式
  const isDark = ref(false)

  // 根据当前状态将 dark 类应用到 <html> 元素
  function applyTheme() {
    document.documentElement.classList.toggle('dark', isDark.value)
  }

  // 初始化主题（应用启动时调用）
  // 优先级：localStorage 保存的偏好 > 系统主题偏好
  function initTheme() {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved === 'dark') {
      isDark.value = true
    } else if (saved === 'light') {
      isDark.value = false
    } else {
      isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    applyTheme()
  }

  // 切换日间/夜间模式并持久化偏好
  function toggle() {
    isDark.value = !isDark.value
    localStorage.setItem(STORAGE_KEY, isDark.value ? 'dark' : 'light')
    applyTheme()
  }

  return { isDark, initTheme, toggle }
})
