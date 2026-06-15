import request from '@/utils/request'

/** 获取分类树 */
export function getCategoryTree() {
  return request({ url: '/categories/tree', method: 'get' })
}

/** 获取所有分类 */
export function getAllCategories() {
  return request({ url: '/categories', method: 'get' })
}

/** 新增分类 */
export function addCategory(data) {
  return request({ url: '/categories', method: 'post', data })
}

/** 更新分类 */
export function updateCategory(id, data) {
  return request({ url: `/categories/${id}`, method: 'put', data })
}

/** 删除分类 */
export function deleteCategory(id) {
  return request({ url: `/categories/${id}`, method: 'delete' })
}
