import request from '@/utils/request'
export const getReviews = (bookId) => request({ url: `/reviews/book/${bookId}`, method: 'get' })
export const getReviewStats = (bookId) => request({ url: `/reviews/stats/${bookId}`, method: 'get' })
export const addReview = (data) => request({ url: '/reviews', method: 'post', data })
export const delReview = (id) => request({ url: `/reviews/${id}`, method: 'delete' })
export const uploadFile = (file) => {
  const fd = new FormData(); fd.append('file', file)
  return request({ url: '/upload', method: 'post', data: fd, headers: { 'Content-Type': 'multipart/form-data' } })
}
