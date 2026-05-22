import axios from 'axios'
import type { ApiResult } from '@/types'
import router from '@/router'

const http = axios.create({
  baseURL: '/api',
  withCredentials: true,
  timeout: 30000,
})

http.interceptors.response.use(
  (res): any => res.data,
  (err) => {
    if (err.response?.status === 401) {
      router.push('/login')
    }
    const msg = err.response?.data?.msg || err.message || '请求失败'
    return Promise.reject(new Error(msg))
  }
)

export default http
