import request from '@/utils/request'

export function toggleFavorite(bookId) {
  return request({ url: `/favorites/toggle/${bookId}`, method: 'post' })
}

export function getFavorites() {
  return request({ url: '/favorites', method: 'get' })
}

export function checkFavorite(bookId) {
  return request({ url: `/favorites/check/${bookId}`, method: 'get' })
}
