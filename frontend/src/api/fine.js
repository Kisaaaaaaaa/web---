import request from '@/utils/request'

/** 分页查询罚金记录 */
export function getFineRecords(params) {
  return request({ url: '/fines', method: 'get', params })
}

/** 查询罚金详情 */
export function getFineDetail(id) {
  return request({ url: `/fines/${id}`, method: 'get' })
}

/** 更新缴费状态（管理员） */
export function updatePaymentStatus(id, paymentStatus) {
  return request({ url: `/fines/${id}/payment`, method: 'put', data: { paymentStatus } })
}

/** 获取未缴罚金总额 */
export function getUnpaidTotal() {
  return request({ url: '/fines/unpaid-total', method: 'get' })
}

/** 罚金状态分布统计 */
export function getFineStatsDistribution() {
  return request({ url: '/fines/stats/distribution', method: 'get' })
}
