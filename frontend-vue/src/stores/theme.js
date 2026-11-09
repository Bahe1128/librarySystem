// 主题 Store - 管理日间/夜间模式
// 偏好保存在 localStorage 中，未设置时跟随系统主题（并监听系统主题实时变化）
// 通过给 <html> 添加 dark 类来切换暗色样式

import { defineStore } from 'pinia'
import { ref } from 'vue'

const STORAGE_KEY = 'library-theme'

export const useThemeStore = defineStore('theme', () => {
  // 当前是否为夜间模式
  const isDark = ref(false)
  // 系统主题监听器引用
  let mql = null

  // 根据当前状态将 dark 类应用到 <html> 元素
  function applyTheme() {
    document.documentElement.classList.toggle('dark', isDark.value)
  }

  // 系统主题变化时跟随；仅当用户未手动设置偏好时生效（手动切换即锁定）
  function onSystemChange(e) {
    if (localStorage.getItem(STORAGE_KEY)) return
    isDark.value = e.matches
    applyTheme()
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
    // 监听系统主题变化，Store 与 App 同生命周期，无需注销
    mql = window.matchMedia('(prefers-color-scheme: dark)')
    if (mql.addEventListener) mql.addEventListener('change', onSystemChange)
  }

  // 切换日间/夜间模式并持久化偏好
  function toggle() {
    isDark.value = !isDark.value
    localStorage.setItem(STORAGE_KEY, isDark.value ? 'dark' : 'light')
    applyTheme()
  }

  return { isDark, initTheme, toggle }
})
