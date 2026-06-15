import request from '@/utils/request'

/** 分页查询图书列表 */
export function getBookList(params) {
  return request({ url: '/books', method: 'get', params })
}

/** 查询图书详情 */
export function getBookDetail(id) {
  return request({ url: `/books/${id}`, method: 'get' })
}

/** 新增图书（管理员） */
export function addBook(data) {
  return request({ url: '/books', method: 'post', data })
}

/** 更新图书（管理员） */
export function updateBook(id, data) {
  return request({ url: `/books/${id}`, method: 'put', data })
}

/** 上架/下架图书（管理员） */
export function updateBookStatus(id, status) {
  return request({ url: `/books/${id}/status`, method: 'put', params: { status } })
}

/** 删除图书（管理员） */
export function deleteBook(id) {
  return request({ url: `/books/${id}`, method: 'delete' })
}
