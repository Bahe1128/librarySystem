// 错误信息提取工具
//
// 统一从任意捕获值中提取可展示的提示文案：
//   - Error 实例取 .message（后端业务错误已由 API 拦截器转为 Error）
//   - 字符串直接返回（如 ElMessageBox 的取消值 'cancel'）
//   - 其余兜底返回 fallback
export function getErrMsg(e, fallback = '操作失败') {
  if (e instanceof Error && e.message) return e.message
  if (typeof e === 'string' && e) return e
  return fallback
}
