<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-brand-primary to-brand-dark px-4 py-10">
    <div class="w-full max-w-sm">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-white tracking-tight">找回密码</h1>
        <p class="mt-2 text-white/70 text-sm">通过邮箱验证码重置你的密码</p>
      </div>
      <div class="card p-6">
        <el-form class="space-y-4">
          <el-form-item label="手机号">
            <el-input v-model="userID" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="email" placeholder="请输入绑定邮箱" />
          </el-form-item>
          <div class="flex gap-2">
            <el-input v-model="code" placeholder="请输入邮箱验证码" class="flex-1" />
            <button type="button" @click="sendCode" class="btn-secondary !px-3 !py-2 text-sm shrink-0">发送验证码</button>
          </div>
          <el-form-item label="新密码">
            <el-input v-model="newPassword" type="password" show-password placeholder="请输入新密码" />
          </el-form-item>
          <button type="button" @click="reset" class="btn-primary w-full justify-center py-2.5 mt-2">重置密码</button>
        </el-form>
        <div class="mt-4 text-center text-xs text-brand-muted">
          <router-link to="/login" class="hover:text-brand-primary transition">返回登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '@/api/http'
import type { ApiResult } from '@/types'

const router = useRouter()
const userID = ref('')
const email = ref('')
const code = ref('')
const newPassword = ref('')

async function sendCode() {
  const body = new URLSearchParams({ userID: userID.value, email: email.value })
  const res = await http.post<any, ApiResult>('/profile/send-reset-code', body)
  res.success ? ElMessage.success(res.msg) : ElMessage.error(res.msg)
}

async function reset() {
  const body = new URLSearchParams({ userID: userID.value, code: code.value, newPassword: newPassword.value })
  const res = await http.post<any, ApiResult>('/profile/reset-password', body)
  if (res.success) { ElMessage.success(res.msg); router.push('/login') }
  else ElMessage.error(res.msg)
}
</script>
