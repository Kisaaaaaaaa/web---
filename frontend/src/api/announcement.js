import request from '@/utils/request'
export const getAnnouncements = () => request({ url: '/announcements', method: 'get' })
export const getAllAnnouncements = () => request({ url: '/announcements/all', method: 'get' })
export const addAnnouncement = (d) => request({ url: '/announcements', method: 'post', data: d })
export const updateAnnouncement = (id, d) => request({ url: `/announcements/${id}`, method: 'put', data: d })
export const delAnnouncement = (id) => request({ url: `/announcements/${id}`, method: 'delete' })
