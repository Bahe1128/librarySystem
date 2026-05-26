// 应用入口文件 - Vue 应用从这里启动

import { createApp } from 'vue'
// 引入 Pinia 状态管理库（Vue 3 推荐的状态管理方案，替代 Vuex）
import { createPinia } from 'pinia'
// 引入 Element Plus UI 组件库
import ElementPlus from 'element-plus'
// 引入 Element Plus 的中文本地化配置
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
// 引入 Element Plus 全局样式
import 'element-plus/dist/index.css'
// 引入 Element Plus 的所有图标组件
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
// 引入根组件
import App from './App.vue'
// 引入路由配置
import router from './router'
// 引入全局自定义样式
import './style.css'

// 创建 Vue 应用实例
const app = createApp(App)

// 注册 Pinia 状态管理
app.use(createPinia())
// 注册 Vue Router 路由
app.use(router)
// 注册 Element Plus 组件库，并设置中文本地化
app.use(ElementPlus, { locale: zhCn })

// 全局注册所有 Element Plus 图标组件
// 这样在模板中可以直接使用 <el-icon><IconName /></el-icon> 而不需要逐个导入
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 将 Vue 应用挂载到 index.html 中的 #app 元素上
app.mount('#app')
