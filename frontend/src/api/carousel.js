import request from '@/utils/request'
export const getCarousels = () => request({ url: '/carousels', method: 'get' })
export const getAllCarousels = () => request({ url: '/carousels/all', method: 'get' })
export const addCarousel = (d) => request({ url: '/carousels', method: 'post', data: d })
export const updateCarousel = (id, d) => request({ url: `/carousels/${id}`, method: 'put', data: d })
export const delCarousel = (id) => request({ url: `/carousels/${id}`, method: 'delete' })
