import request from '@/utils/request'

export function getBorrowRecords(params) {
  return request({ url: '/borrows', method: 'get', params })
}
export function borrowBook(bookId) {
  return request({ url: '/borrows', method: 'post', params: { bookId } })
}
export function returnBook(id) {
  return request({ url: `/borrows/${id}/return`, method: 'put' })
}
export function renewBook(id) {
  return request({ url: `/borrows/${id}/renew`, method: 'put' })
}
export function markOverdue(id) {
  return request({ url: `/borrows/${id}/overdue`, method: 'put' })
}
export function getBorrowStats() {
  return request({ url: '/borrows/stats', method: 'get' })
}
export function getTopBooks(limit = 10) {
  return request({ url: '/borrows/top', method: 'get', params: { limit } })
}
export function getStatsByCategory() {
  return request({ url: '/borrows/stats/category', method: 'get' })
}
export function getDailyStats() {
  return request({ url: '/borrows/stats/daily', method: 'get' })
}
export function getStatusDistribution() {
  return request({ url: '/borrows/stats/distribution', method: 'get' })
}
