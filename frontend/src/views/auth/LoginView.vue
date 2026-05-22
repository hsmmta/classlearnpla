<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-brand-primary to-brand-dark px-4 py-10">
    <div class="w-full max-w-sm">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-white tracking-tight">班级学习社区</h1>
        <p class="mt-2 text-white/70 text-sm">欢迎回来，请登录你的账号</p>
      </div>
      <div class="card p-6">
        <el-tabs v-model="loginType" class="mb-4">
          <el-tab-pane label="密码登录" name="password" />
          <el-tab-pane label="验证码登录" name="code" />
        </el-tabs>
        <el-form @submit.prevent="onSubmit" class="space-y-4">
          <el-form-item label="手机号">
            <el-input v-model="form.userID" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item v-if="loginType === 'password'" label="密码">
            <el-input v-model="form.userPassword" type="password" show-password placeholder="请输入密码" />
          </el-form-item>
          <el-form-item v-else label="验证码">
            <div class="flex gap-2 w-full">
              <el-input v-model="form.code" placeholder="请输入验证码" class="flex-1" />
              <button type="button" :disabled="countdown > 0" @click="sendCode"
                class="btn-secondary !px-3 !py-2 text-sm shrink-0 disabled:opacity-50 disabled:cursor-not-allowed">
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </button>
            </div>
          </el-form-item>
          <button type="submit" class="btn-primary w-full justify-center py-2.5 mt-2">登录</button>
        </el-form>
        <div class="mt-4 flex justify-between text-xs text-brand-muted">
          <router-link to="/register" class="hover:text-brand-primary transition">注册账号</router-link>
          <router-link to="/profile/forgot" class="hover:text-brand-primary transition">忘记密码</router-link>
          <router-link to="/admin/login" class="hover:text-brand-primary transition">管理员登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '@/api/http'
import { useUserStore } from '@/stores/user'
import type { ApiResult, SessionUser } from '@/types'

const router = useRouter()
const store = useUserStore()
const loginType = ref('password')
const countdown = ref(0)
const form = reactive({ userID: '', userPassword: '', code: '' })

async function sendCode() {
  if (!form.userID) return ElMessage.warning('请输入手机号')
  const res = await http.post<any, ApiResult>('/auth/send-code', new URLSearchParams({ phone: form.userID }))
  if (res.success) {
    ElMessage.success(res.msg)
    countdown.value = 60
    const t = setInterval(() => { if (--countdown.value <= 0) clearInterval(t) }, 1000)
  } else ElMessage.error(res.msg)
}

async function onSubmit() {
  const body = new URLSearchParams({ userID: form.userID, loginType: loginType.value })
  if (loginType.value === 'password') body.append('userPassword', form.userPassword)
  else body.append('code', form.code)
  const res = await http.post<any, ApiResult<SessionUser>>('/auth/login', body)
  if (res.success) {
    store.setUser(res.data as SessionUser)
    ElMessage.success('登录成功')
    router.push(res.data?.userType === 'admin' ? '/admin' : '/')
  } else ElMessage.error(res.msg)
}
</script>
