import { defineStore } from 'pinia'
import { ref } from 'vue'
import http from '@/api/http'
import type { ApiResult, SessionUser } from '@/types'

export const useUserStore = defineStore('user', () => {
  const user = ref<SessionUser | null>(null)
  const loaded = ref(false)

  async function fetchSession() {
    try {
      const res = await http.get<any, ApiResult<SessionUser>>('/session')
      if (res.success && res.data) {
        user.value = res.data as SessionUser
      } else {
        user.value = null
      }
    } catch {
      user.value = null
    } finally {
      loaded.value = true
    }
  }

  function setUser(u: SessionUser | null) {
    user.value = u
    loaded.value = true
  }

  async function logout() {
    try {
      await http.post('/auth/logout')
    } finally {
      user.value = null
    }
  }

  return { user, loaded, fetchSession, setUser, logout }
})
