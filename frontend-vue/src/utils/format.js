// 格式化工具

// 格式化日期为中文格式（年-月-日），无效或空值返回 '-'
export function formatDate(date) {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}
