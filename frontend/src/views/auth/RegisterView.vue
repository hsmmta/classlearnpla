<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-brand-primary to-brand-dark px-4 py-10">
    <div class="w-full max-w-sm">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-white tracking-tight">创建账号</h1>
        <p class="mt-2 text-white/70 text-sm">加入班级学习社区</p>
      </div>
      <div class="card p-6">
        <el-form @submit.prevent="onSubmit" class="space-y-3">
          <el-form-item label="手机号">
            <el-input v-model="form.userID" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="form.userName" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.userPassword" type="password" show-password placeholder="请输入密码" />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.userEmail" placeholder="请输入邮箱（选填）" />
          </el-form-item>
          <el-form-item label="验证码">
            <div class="flex gap-2 w-full">
              <el-input v-model="form.code" placeholder="请输入验证码" class="flex-1" />
              <button type="button" :disabled="countdown > 0" @click="sendCode"
                class="btn-secondary !px-3 !py-2 text-sm shrink-0 disabled:opacity-50">
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </button>
            </div>
          </el-form-item>
          <button type="submit" class="btn-primary w-full justify-center py-2.5 mt-2">注册</button>
        </el-form>
        <div class="mt-4 text-center text-xs text-brand-muted">
          已有账号？
          <router-link to="/login" class="text-brand-primary hover:underline">立即登录</router-link>
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
import type { ApiResult } from '@/types'

const router = useRouter()
const countdown = ref(0)
const form = reactive({ userID: '', userName: '', userPassword: '', confirmPassword: '', userEmail: '', code: '' })

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
  if (!form.userPassword || !form.confirmPassword) {
    ElMessage.warning('请输入并确认密码')
    return
  }
  if (form.userPassword !== form.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  const body = new URLSearchParams({
    userID: form.userID,
    userName: form.userName,
    userPassword: form.userPassword,
    repassword: form.confirmPassword,
    userEmail: form.userEmail,
    code: form.code,
  })
  const res = await http.post<any, ApiResult>('/auth/register', body)
  if (res.success) { ElMessage.success('注册成功，请登录'); router.push('/login') }
  else ElMessage.error(res.msg)
}
</script>
