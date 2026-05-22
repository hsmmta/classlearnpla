<template>
  <div class="page-shell">
    <PageHeader title="修改密码" />
    <div class="card p-6 max-w-md">
      <el-form label-width="90px" @submit.prevent="submit" class="space-y-2">
        <el-form-item label="原密码"><el-input v-model="form.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="form.newPassword" type="password" show-password /></el-form-item>
        <div class="flex gap-3 pt-2">
          <button type="submit" class="btn-primary"><Lock class="h-4 w-4" /> 确认修改</button>
          <button type="button" class="btn-ghost" @click="$router.back()">取消</button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Lock } from '@lucide/vue'
import http from '@/api/http'
import type { ApiResult } from '@/types'
import PageHeader from '@/components/PageHeader.vue'

const form = reactive({ oldPassword: '', newPassword: '' })

async function submit() {
  const body = new URLSearchParams(form as any)
  const res = await http.post<any, ApiResult>('/profile/password', body)
  res.success ? ElMessage.success(res.msg) : ElMessage.error(res.msg)
}
</script>
