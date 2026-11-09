# Vue 前端说明

> 以本项目的实际代码为例，介绍前端的技术栈、目录结构、核心机制与页面组成。

---

## 1. 技术栈总览

| 技术 | 版本 | 在本项目中的职责 |
|------|------|------------------|
| Vue 3 | ^3.4 | 视图框架，全部使用 `<script setup>` 组合式 API |
| Vite | ^5.0 | 开发服务器与构建工具，提供热更新和 `/api` 代理 |
| Vue Router | ^4.2 | 页面路由、懒加载、登录/管理员权限守卫 |
| Pinia | ^2.1 | 全局状态管理（登录状态、主题模式） |
| Element Plus | ^2.5 | UI 组件库（表格、弹窗、表单、分页、消息提示） |
| Axios | ^1.6 | HTTP 请求，统一拦截器与错误处理 |
| @element-plus/icons-vue | ^2.3 | 图标组件，在 `main.js` 中全局注册 |

构建与运行（在 `frontend-vue/` 目录下）：

```bash
npm install
npm run dev      # 开发服务器，默认 http://localhost:5173
npm run build    # 生产构建，输出到 dist/
npm run preview  # 本地预览构建产物
```

---

## 2. 目录结构

```
frontend-vue/
├── index.html                 # HTML 模板，挂载点 #app
├── vite.config.js             # Vite 配置（@ 别名、5173 端口、/api 代理）
├── package.json
└── src/
    ├── main.js                # 应用入口：注册 Pinia / Router / Element Plus / 图标
    ├── App.vue                # 根组件：Navbar + router-view + 页面过渡
    ├── style.css              # 全局设计变量、按钮原语、Element Plus 样式覆盖
    ├── api/
    │   └── index.js           # Axios 实例 + 请求/响应拦截器 + 所有接口封装
    ├── router/
    │   └── index.js           # 路由表、懒加载、权限守卫、标题设置
    ├── stores/
    │   ├── auth.js            # 登录状态、Token、用户信息（localStorage 持久化）
    │   └── theme.js           # 日间/夜间模式（跟随系统 + 手动锁定）
    ├── composables/
    │   └── useBookshelf.js    # 书架页的全部状态与交互逻辑
    ├── utils/
    │   ├── error.js           # 从异常中提取可展示的错误文案
    │   └── format.js          # 日期格式化
    ├── components/
    │   ├── Navbar.vue         # 顶部导航（含移动端抽屉）
    │   ├── AuthForm.vue       # 登录/注册共用表单
    │   ├── BookPopover.vue    # 书架悬浮详情卡
    │   ├── PageHeader.vue     # 页面标题 + 描述 + 右侧操作区
    │   ├── TableCard.vue      # 表格卡片容器
    │   └── EmptyState.vue     # 统一空状态
    └── views/
        ├── Home.vue           # 首页：Hero + 统计卡片 + 功能网格
        ├── Books.vue          # 图书书架：分类书脊 + 搜索 + 借阅
        ├── Login.vue          # 登录页（AuthForm mode="login"）
        ├── Register.vue       # 注册页（AuthForm mode="register"）
        ├── BorrowRecords.vue  # 借阅记录：表格 + 分页 + 归还
        ├── Admin.vue          # 后台管理：图书增删改查
        ├── AdminAccounts.vue  # 账号管理：用户增删改查
        └── NotFound.vue       # 404 页面
```

---

## 3. 应用入口 `main.js`

**文件：** [main.js](frontend-vue/src/main.js)

```js
const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
useThemeStore(pinia).initTheme()   // 渲染前初始化主题，避免闪烁
app.use(router)
app.use(ElementPlus, { locale: zhCn })

// 全局注册所有图标，模板中可直接 <el-icon><Reading /></el-icon>
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
```

几个值得注意的点：

- **主题先于挂载初始化**：`initTheme()` 在 `app.mount()` 之前调用，给 `<html>` 加上 `dark` 类，避免首屏先亮后暗的闪烁。
- **图标全局注册**：因此 `EmptyState` 可以通过字符串 `icon="FolderOpened"` + `<component :is="icon" />` 动态渲染图标。
- **Element Plus 中文化**：`zhCn` 让分页、表格等内置文案显示中文。

---

## 4. 根组件 `App.vue`

**文件：** [App.vue](frontend-vue/src/App.vue)

```vue
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
```

- `Navbar` 在所有页面常驻，页面内容通过 `router-view` 渲染。
- `<transition mode="out-in">` 实现路由切换时的淡入淡出 + 轻微位移（`.page-*` 样式）。
- `main-container` 限制最大宽度 1280px，移动端收窄内边距。

---

## 5. 路由 `router/index.js`

**文件：** [router/index.js](frontend-vue/src/router/index.js)

### 路由表

| 路径 | 组件 | 访问权限 | meta.title |
|------|------|----------|------------|
| `/` | Home | 公开 | 首页 |
| `/books` | Books | 公开 | 图书书架 |
| `/login` | Login | 公开 | 登录 |
| `/register` | Register | 公开 | 注册 |
| `/borrow-records` | BorrowRecords | 需登录 | 借阅记录 |
| `/admin` | Admin | 需登录 + 管理员 | 后台管理 |
| `/admin/accounts` | AdminAccounts | 需登录 + 管理员 | 账号管理 |
| `/:pathMatch(.*)*` | NotFound | 公开 | 页面不存在 |

所有页面组件都用 `() => import(...)` **懒加载**，Vite 会自动按路由分包，减小首屏体积。

### 权限守卫

```js
router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin && auth.user?.role !== 'admin') {
    return { name: 'Home' }
  }
  return true
})
```

- 未登录访问受保护页面 → 跳登录页，并把原地址放进 `?redirect=`，登录成功后由 `AuthForm` 跳回。
- 普通用户访问管理页 → 重定向首页。
- `afterEach` 根据 `meta.title` 设置浏览器标题，例如「图书书架 · 图书管理系统」。

> 注意：前端守卫只是体验层拦截，真正的权限控制在后端 `SecurityConfig`。即使绕过前端直接调接口，后端仍会返回 401/403。

---

## 6. API 层 `api/index.js`

**文件：** [api/index.js](frontend-vue/src/api/index.js)

### 统一响应约定

后端所有接口返回 `{ code, message, data }`，成功时 `code = 200`。

```js
const request = axios.create({
  baseURL: '/api',   // 与 Vite 代理配合，转发到 http://localhost:5000
  timeout: 10000
})
```

### 请求拦截器：自动携带 JWT

```js
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
```

### 响应拦截器：解包 + 统一错误

```js
response => {
  const body = response.data
  if (body && body.code === 200) return body.data   // 成功直接返回 data
  // 业务码 401（且不是登录/注册接口）→ 会话过期
  if (body?.code === 401 && !AUTH_PATHS.some(p => url.includes(p))) {
    redirectToLogin()
  }
  return Promise.reject(new Error(body?.message || '请求失败'))
}
```

这套设计带来两个好处：

1. **业务层无需重复解包**：调用 `bookAPI.getAll()` 拿到的直接就是 `{ records, total, ... }`，不用再写 `res.data.data`。
2. **错误统一为 `Error(message)`**：视图层只需 `catch (e) { ElMessage.error(getErrMsg(e)) }`，提示文案来自后端。

### 接口分组

| 对象 | 代表方法 | 对应后端 |
|------|----------|----------|
| `authAPI` | `login` / `register` | `/api/auth/**` |
| `bookAPI` | `getAll` / `getAllFlat` / `search` / `create` / `update` / `delete` | `/api/books/**` |
| `borrowAPI` | `borrow` / `getRecords` / `returnBook` | `/api/borrow/**` |
| `userAPI` | `getAll` / `create` / `update` / `delete` | `/api/users/**` |
| `statsAPI` | `getStats` / `getTrends` | `/api/stats/**` |

### 会话过期处理

```js
function redirectToLogin() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  const redirect = encodeURIComponent(location.pathname + location.search)
  location.href = `/login?redirect=${redirect}`
}
```

直接操作 `localStorage` 而不是调用 Pinia store，是为了避免模块循环依赖（`api` 被 store 引用）。

---

## 7. 状态管理 Pinia

### `stores/auth.js` —— 登录状态

**文件：** [stores/auth.js](frontend-vue/src/stores/auth.js)

```js
export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const isLoggedIn = computed(() => !!token.value)

  function setAuth(newToken, newUser) {
    if (!newToken) throw new Error('登录失败：未获取到凭证')  // 防御性检查
    token.value = newToken
    user.value = newUser
    localStorage.setItem('token', newToken)
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  async function login(username, password) {
    const res = await authAPI.login(username, password)   // 拦截器已解包
    setAuth(res.token, res.user)
  }

  function logout() { /* 清空内存与 localStorage */ }

  return { token, user, isLoggedIn, login, register, logout, setAuth }
})
```

要点：

- 使用 **setup 语法**定义 store，`ref` 是 state，`computed` 是 getter，函数是 action。
- Token 与用户信息持久化到 `localStorage`，刷新页面后自动恢复登录态。
- `setAuth` 对空 token 做防御性检查，避免「登录失败却写入本地存储」。
- 刷新后是否仍有效，最终由后端校验 JWT 决定；过期时拦截器会清理并跳登录。

### `stores/theme.js` —— 日间/夜间模式

**文件：** [stores/theme.js](frontend-vue/src/stores/theme.js)

```js
function initTheme() {
  const saved = localStorage.getItem(STORAGE_KEY)
  if (saved === 'dark') isDark.value = true
  else if (saved === 'light') isDark.value = false
  else isDark.value = matchMedia('(prefers-color-scheme: dark)').matches

  applyTheme()   // document.documentElement.classList.toggle('dark', isDark)
  // 未手动设置偏好时，跟随系统主题实时变化
}
```

优先级：**用户手动选择 > 系统主题**。手动切换后会写入 `localStorage`，系统主题变化不再影响页面。

---

## 8. 页面说明

### 首页 `Home.vue`

**文件：** [views/Home.vue](frontend-vue/src/views/Home.vue)

- Hero 区：标语 + 「浏览图书」/「立即登录」按钮。
- 统计卡片：馆藏图书、借出中、逾期未还、注册用户。
- 功能网格：6 张功能介绍卡片。
- 统计数据需要登录：未登录时不请求，避免 401 被踢回登录页；请求失败也静默处理，不影响页面展示。

```js
onMounted(async () => {
  if (!auth.isLoggedIn) return
  try { stats.value = await statsAPI.getStats() } catch (_) {}
})
```

### 图书书架 `Books.vue`

**文件：** [views/Books.vue](frontend-vue/src/views/Books.vue)

项目的核心页面，交互逻辑较多，因此抽到了组合式函数 `useBookshelf`：

- 顶部搜索框：**客户端即时过滤**，按书名 / 作者 / ISBN / 分类匹配，不发请求。
- 书脊字号调节（9–20px），同时缩放书脊宽度、高度和间距，设置持久化到 `localStorage`。
- 分类导航 chips：sticky 吸顶，点击平滑滚动到对应书架；滚动时高亮当前分类。
- 每本书渲染为一根彩色书脊，颜色由分类色相决定，高度随书名长度变化。
- 鼠标悬浮 200ms 弹出详情卡；移出 200ms 后隐藏，给用户时间移入卡片。
- 卡片自动判断显示在书脊上方还是下方，避免超出视口。
- 借阅成功本地 `availableCopies -= 1`，删除成功本地移除，避免整页重新请求。

### 借阅记录 `BorrowRecords.vue`

**文件：** [views/BorrowRecords.vue](frontend-vue/src/views/BorrowRecords.vue)

- 表格展示借阅日期、应还日期、归还日期、状态。
- 普通用户只看自己的记录；管理员额外显示「借阅人」列，并看到全库记录（后端按角色过滤）。
- 状态根据后端返回的 `overdue` 字段显示：已逾期 / 借阅中 / 已归还。
- 支持分页（10/20/50/100）和「归还」操作。

### 后台管理 `Admin.vue`

**文件：** [views/Admin.vue](frontend-vue/src/views/Admin.vue)

- 图书表格：ID、书名、作者、ISBN、分类、可用/总库存。
- 新增/编辑共用同一个 `el-dialog`，`editingId` 为 `null` 表示新增。
- 表单校验交给 Element Plus 的 `el-form` + `rules`。
- 删除前用 `ElMessageBox.confirm` 二次确认；用户点取消时不弹错误提示。

### 账号管理 `AdminAccounts.vue`

**文件：** [views/AdminAccounts.vue](frontend-vue/src/views/AdminAccounts.vue)

- 只管理普通用户（后端 `findAll` 已排除管理员）。
- 编辑时密码留空表示不修改：提交前 `if (!payload.password) delete payload.password`。
- 删除提示中明确说明：已归还记录会一并删除，存在未归还图书时无法删除。

### 登录 / 注册

**文件：** [views/Login.vue](frontend-vue/src/views/Login.vue)、[views/Register.vue](frontend-vue/src/views/Register.vue)

两个页面都只是对 `AuthForm` 的一行封装：

```vue
<template><AuthForm mode="login" /></template>
```

### 404 `NotFound.vue`

由路由的 catch-all 规则 `/:pathMatch(.*)*` 捕获，提供返回首页入口。

---

## 9. 通用组件

| 组件 | 文件 | 作用 |
|------|------|------|
| `Navbar` | [components/Navbar.vue](frontend-vue/src/components/Navbar.vue) | 顶部导航：品牌、链接、用户菜单、主题切换、移动端抽屉 |
| `AuthForm` | [components/AuthForm.vue](frontend-vue/src/components/AuthForm.vue) | 登录/注册共用表单，`mode` prop 区分模式 |
| `BookPopover` | [components/BookPopover.vue](frontend-vue/src/components/BookPopover.vue) | 书架悬浮详情卡，父级负责定位，组件只负责内容与事件 |
| `PageHeader` | [components/PageHeader.vue](frontend-vue/src/components/PageHeader.vue) | 统一页面标题、描述与右侧操作插槽 |
| `TableCard` | [components/TableCard.vue](frontend-vue/src/components/TableCard.vue) | 圆角卡片容器，包裹表格 |
| `EmptyState` | [components/EmptyState.vue](frontend-vue/src/components/EmptyState.vue) | 统一「无数据」展示，支持图标、标题、描述和插槽 |

### `AuthForm` 的 props / 事件

```js
const props = defineProps({
  mode: { type: String, default: 'login' }   // 'login' | 'register'
})
```

登录成功后读取 `route.query.redirect` 跳回原页面：

```js
const redirect = typeof route.query.redirect === 'string' && route.query.redirect
  ? route.query.redirect
  : '/'
router.push(redirect)
```

### `BookPopover` 的 `defineExpose`

父级需要测量卡片尺寸来计算定位，因此组件暴露根元素：

```js
const root = ref(null)
defineExpose({
  get el() { return root.value }   // 用 getter 保证返回 DOM 节点本身
})
```

---

## 10. 组合式函数 `useBookshelf`

**文件：** [composables/useBookshelf.js](frontend-vue/src/composables/useBookshelf.js)

`Books.vue` 把状态与交互全部交给这个函数，组件本身只保留模板和样式。

### 输入与输出

```js
const { keyword, fontSize, loading, shelves, activeBook, spineStyle,
        startHover, startHide, handleBorrow, handleDelete } = useBookshelf(popoverRef)
```

参数 `popoverRef` 是 `BookPopover` 的组件引用，用于测量卡片宽高后计算坐标。

### 核心逻辑

| 逻辑 | 实现 |
|------|------|
| 搜索过滤 | `searchResults` 计算属性，内存过滤，不请求后端 |
| 分类分组 | `buildShelves()`：按 `category` 分组，按 `CATEGORY_ORDER` 排序 |
| 每行容量 | `computeBooksPerRow()`：测量实际行宽 ÷（书脊宽 + 间距） |
| 书脊样式 | `spineStyle()`：分类色相 + 饱和度/亮度微调，高度随书名长度变化 |
| 悬浮卡定位 | `showPopover()`：`getBoundingClientRect` + 视口边界避让 |
| 滚动高亮 | `onScroll()`：找到最后一个顶部越过阈值的分类区块 |
| 响应式 | `ResizeObserver` 监听容器宽度，窗口 `resize` 时关闭卡片 |

### 悬浮卡防闪烁

```js
function startHover(book, event) {
  cancelHide()
  clearTimeout(hoverTimer)
  hoverTimer = setTimeout(() => showPopover(book, event.currentTarget), 200)
}
function startHide() {
  clearTimeout(hoverTimer)
  hideTimer = setTimeout(() => { activeBook.value = null }, 200)
}
```

进入和离开都延迟 200ms：快速扫过书脊不会闪出卡片，鼠标从书脊移到卡片上也不会中途消失。

### 生命周期清理

```js
onBeforeUnmount(() => {
  ro?.disconnect()
  window.removeEventListener('scroll', onScroll)
  window.removeEventListener('resize', closePopover)
  clearTimeout(hoverTimer)
  clearTimeout(hideTimer)
})
```

---

## 11. 工具函数

| 文件 | 方法 | 说明 |
|------|------|------|
| [utils/error.js](frontend-vue/src/utils/error.js) | `getErrMsg(e, fallback)` | `Error` 取 `message`，字符串原样返回，其余用兜底文案 |
| [utils/format.js](frontend-vue/src/utils/format.js) | `formatDate(date)` | 输出 `2026/01/01` 形式的中文日期，空值返回 `-` |

统一错误提取让所有视图的写法保持一致：

```js
try { await bookAPI.delete(id) }
catch (e) { if (e !== 'cancel') ElMessage.error(getErrMsg(e, '删除失败')) }
```

---

## 12. 样式系统

**文件：** [style.css](frontend-vue/src/style.css)

设计参考 Untitled UI，所有组件通过 CSS 变量取色，因此暗色模式只需覆盖变量。

### 设计变量

| 类别 | 变量示例 | 用途 |
|------|----------|------|
| 主色 | `--color-primary`、`--color-primary-50`…`900` | 品牌紫，按钮、链接、高亮 |
| 中性色 | `--gray-25`…`--gray-900` | 文字、边框、背景 |
| 语义色 | `--color-success/warning/danger-*` | 成功、警告、危险状态 |
| 背景/表面 | `--color-bg`、`--color-surface` | 页面背景、卡片背景 |
| 文字 | `--color-text`、`--color-text-secondary`、`--color-text-tertiary` | 三级文字层次 |
| 阴影 | `--shadow-xs`…`--shadow-3xl` | 分层阴影 |
| 圆角 | `--radius-sm`…`--radius-2xl`、`--radius-full` | 统一圆角 |
| 过渡 | `--transition-fast`、`--transition` | 150ms / 200ms 动效 |
| 焦点环 | `--focus-ring` | 键盘可访问性 |

### 暗色模式

```css
html.dark {
  color-scheme: dark;
  --color-bg: #0b0d11;
  --color-surface: #13161d;
  --color-primary: var(--color-primary-400);
  /* ... */
}
```

`theme.js` 只负责给 `<html>` 切换 `dark` 类，其它样式自动生效。个别硬编码颜色（如书架的木质层板）在组件内用 `html.dark .books-page { ... }` 局部覆盖。

### 按钮原语

`style.css` 定义了全局按钮类，各视图直接复用：

```html
<button class="btn btn-primary">确认</button>
<button class="btn btn-ghost">取消</button>
<button class="btn btn-danger">删除</button>
```

### Element Plus 覆盖

通过 CSS 变量和少量 `!important`，让 Element Plus 的表格、分页、输入框、弹窗、下拉菜单与整体设计语言一致。

### 响应式与可访问性

- 断点：`1024px`（统计卡两列）、`768px`（导航折叠、书页面板收窄）、`640px`（页头纵排）。
- `@media (prefers-reduced-motion: reduce)` 全局关闭动画和过渡。
- 图标按钮带 `aria-label`，键盘焦点使用统一 `focus-ring`。

---

## 13. 典型数据流

### 登录流程

```
AuthForm 提交
  → auth.login(username, password)
    → authAPI.login → POST /api/auth/login
      → 响应拦截器解包 { token, user }
    → setAuth() 写入 Pinia + localStorage
  → 读取 route.query.redirect
  → router.push(原页面或首页)
```

### 借阅流程

```
点击书脊 → BookPopover「借阅」
  → useBookshelf.handleBorrow(book)
    → 未登录？提示并返回
    → borrowAPI.borrow(book.id) → POST /api/borrow
      → 后端：用户行锁 → 上限检查 → 原子扣库存 → 写记录
    → 成功：本地 availableCopies -= 1，提示「借阅成功」
    → 失败：ElMessage.error(后端错误信息)
```

### 会话过期流程

```
任意请求返回业务码 401 或 HTTP 401
  → 响应拦截器 redirectToLogin()
  → 清除 localStorage 的 token / user
  → 跳转 /login?redirect=当前地址
```

---

## 14. 前后端联调要点

- **开发环境跨域**：前端请求统一走 `baseURL: '/api'`，由 Vite 的 `server.proxy` 转发到 `http://localhost:5000`，浏览器不直接跨域。
- **认证方式**：JWT 放在 `Authorization: Bearer <token>` 请求头，由请求拦截器自动添加。
- **响应契约**：后端 `Result<T>` 的 `{ code, message, data }` 是前后端唯一约定，前端拦截器集中处理。
- **角色控制**：前端用 `auth.user.role === 'admin'` 控制菜单和按钮显示；后端用 `SecurityConfig` 的 `hasRole("admin")` 做最终校验。
- **分页字段**：后端 `PageResult` 返回 `{ records, total, page, pageSize }`，直接对应 Element Plus 表格与分页组件。

---

## 15. 新增页面检查清单

1. 在 `views/` 下新建组件，使用 `<script setup>`。
2. 在 `router/index.js` 添加路由，按需设置 `meta.requiresAuth` / `meta.requiresAdmin` / `meta.title`。
3. 接口调用统一放到 `api/index.js` 对应分组中，不要在组件里直接写 `axios`。
4. 需要共享的状态放 `stores/`，页面局部状态用 `ref` / `reactive`。
5. 复用 `PageHeader`、`TableCard`、`EmptyState` 保持视觉一致。
6. 错误提示统一用 `getErrMsg(e, '兜底文案')`。
7. 颜色、圆角、阴影使用 `style.css` 中的 CSS 变量，并检查暗色模式表现。
8. 移动端在 768px 以下检查布局，必要时补充媒体查询。
