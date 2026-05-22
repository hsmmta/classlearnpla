<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-brand-dark to-gray-900 px-4 py-10">
    <div class="w-full max-w-sm">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-white tracking-tight">管理员登录</h1>
        <p class="mt-2 text-white/60 text-sm">班级学习社区后台管理</p>
      </div>
      <div class="card p-6">
        <el-form @submit.prevent="onSubmit" class="space-y-4">
          <el-form-item label="账号">
            <el-input v-model="form.adminID" placeholder="请输入管理员账号" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.adminPassword" type="password" show-password placeholder="请输入密码" />
          </el-form-item>
          <button type="submit" class="btn-primary w-full justify-center py-2.5 mt-2">登录</button>
        </el-form>
        <div class="mt-4 text-center text-xs text-brand-muted">
          <router-link to="/login" class="hover:text-brand-primary transition">返回用户登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '@/api/http'
import { useUserStore } from '@/stores/user'
import type { ApiResult, SessionUser } from '@/types'

const router = useRouter()
const store = useUserStore()
const form = reactive({ adminID: '', adminPassword: '' })

async function onSubmit() {
  const body = new URLSearchParams(form as any)
  const res = await http.post<any, ApiResult<SessionUser>>('/auth/admin-login', body)
  if (res.success) {
    store.setUser(res.data as SessionUser)
    router.push('/admin')
  } else ElMessage.error(res.msg)
}
</script>
