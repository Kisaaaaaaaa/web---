import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const avatarUrl = ref(localStorage.getItem('avatar') || '')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.roleCode === 'ROLE_ADMIN')
  const realName = computed(() => userInfo.value?.realName || '用户')

  async function doLogin(username, password) {
    const res = await loginApi({ username, password })
    const data = res.data
    token.value = data.token
    userInfo.value = {
      userId: data.userId, username: data.username,
      realName: data.realName, roleCode: data.roleCode,
    }
    // token 必须先写入 localStorage，否则后续请求的拦截器读不到
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    // 登录后从后端拉头像
    try {
      const { default: request } = await import('@/utils/request')
      const r = await request({ url: '/users/me', method: 'get' })
      avatarUrl.value = r.data?.avatarUrl || ''
      localStorage.setItem('avatar', avatarUrl.value)
    } catch {}
    return data
  }

  function setAvatar(url) {
    avatarUrl.value = url
    localStorage.setItem('avatar', url)
  }

  async function register(formData) {
    await registerApi(formData)
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    avatarUrl.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('avatar')
  }

  return { token, userInfo, avatarUrl, isLoggedIn, isAdmin, realName, doLogin, setAvatar, register, logout }
})
