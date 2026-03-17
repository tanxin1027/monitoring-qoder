import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import Cookies from 'js-cookie'
import { login, getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(Cookies.get('token') || '')
  const userInfo = ref(null)
  const roles = ref([])
  const permissions = ref([])

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')
  const realName = computed(() => userInfo.value?.realName || '')

  // Actions
  const setToken = (newToken) => {
    token.value = newToken
    Cookies.set('token', newToken, { expires: 1 })
  }

  const clearToken = () => {
    token.value = ''
    userInfo.value = null
    roles.value = []
    permissions.value = []
    Cookies.remove('token')
  }

  const loginAction = async (loginData) => {
    const res = await login(loginData)
    if (res.code === 200) {
      setToken(res.data.token)
      userInfo.value = {
        username: res.data.username,
        realName: res.data.realName
      }
      roles.value = res.data.roles || []
      permissions.value = res.data.permissions || []
      return true
    }
    return false
  }

  const logout = () => {
    clearToken()
  }

  const fetchUserInfo = async () => {
    try {
      const res = await getUserInfo()
      if (res.code === 200) {
        userInfo.value = res.data
        roles.value = res.data.roles || []
        permissions.value = res.data.permissions || []
      }
    } catch (error) {
      console.error('获取用户信息失败', error)
    }
  }

  const hasPermission = (permission) => {
    return permissions.value.includes(permission)
  }

  return {
    token,
    userInfo,
    roles,
    permissions,
    isLoggedIn,
    username,
    realName,
    setToken,
    clearToken,
    loginAction,
    logout,
    fetchUserInfo,
    hasPermission
  }
})
