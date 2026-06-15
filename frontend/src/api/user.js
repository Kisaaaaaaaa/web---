import request from '@/utils/request'

/** 用户列表（管理员） */
export function getUserList(params) {
  return request({ url: '/users', method: 'get', params })
}

/** 用户详情 */
export function getUserDetail(id) {
  return request({ url: `/users/${id}`, method: 'get' })
}

/** 更新用户状态 */
export function updateUserStatus(id, status) {
  return request({ url: `/users/${id}/status`, method: 'put', params: { status } })
}

/** 更新用户信息 */
export function updateUser(id, data) {
  return request({ url: `/users/${id}`, method: 'put', data })
}
