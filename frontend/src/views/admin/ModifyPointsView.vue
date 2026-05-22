<template>
  <div class="page-shell">
    <PageHeader title="积分管理" description="查询并调整用户积分" />
    <div class="grid gap-6 lg:grid-cols-2">
      <div class="card p-5">
        <h3 class="font-semibold text-gray-800 text-sm mb-4">查询用户积分</h3>
        <div class="flex gap-2 mb-4">
          <el-input v-model="userID" placeholder="请输入用户手机号" class="flex-1" />
          <button class="btn-ghost" @click="query">
            <Search class="h-4 w-4" /> 查询
          </button>
        </div>
        <div v-if="points !== null" class="rounded-lg bg-brand-primary/5 border border-brand-primary/20 p-4 text-center">
          <p class="text-sm text-brand-muted">当前积分</p>
          <p class="text-3xl font-bold text-brand-primary mt-1">{{ points }}</p>
        </div>
        <div v-if="warningCount !== null" class="mt-3 rounded-lg border border-red-200 bg-red-50 p-3 text-sm">
          <p class="text-red-600">警告次数：<strong>{{ warningCount }}</strong></p>
          <p class="text-red-500 mt-1" v-if="bannedUntil">封禁至：{{ bannedUntil }}</p>
          <p class="text-green-600 mt-1" v-else>当前未封禁</p>
        </div>
      </div>

      <div class="card p-5">
        <h3 class="font-semibold text-gray-800 text-sm mb-4">修改积分</h3>
        <el-form label-width="80px" class="space-y-2">
          <el-form-item label="操作">
            <el-select v-model="operation">
              <el-option label="增加" value="add" />
              <el-option label="减少" value="subtract" />
              <el-option label="设置" value="set" />
            </el-select>
          </el-form-item>
          <el-form-item label="数量">
            <el-input v-model="pointAmount" type="number" />
          </el-form-item>
          <el-form-item label="原因">
            <el-input v-model="reason" placeholder="管理员调整" />
          </el-form-item>
          <button class="btn-primary" type="button" @click="modify">
            <Coins class="h-4 w-4" /> 提交修改
          </button>
        </el-form>
        <div class="mt-5 pt-4 border-t border-gray-100">
          <h4 class="text-sm font-semibold text-gray-800 mb-3">账号封禁管理</h4>
          <div class="flex flex-wrap items-center gap-2">
            <el-input v-model="banDays" type="number" class="!w-24" placeholder="天数" />
            <button class="btn-secondary !px-3 !py-2 text-xs" type="button" @click="banUser">封禁用户</button>
            <button class="btn-ghost !px-3 !py-2 text-xs" type="button" @click="unbanUser">解封用户</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Search, Coins } from '@lucide/vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'
import type { ApiResult } from '@/types'
import PageHeader from '@/components/PageHeader.vue'

const userID = ref('')
const points = ref<number | null>(null)
const operation = ref('add')
const pointAmount = ref('10')
const reason = ref('管理员调整')
const warningCount = ref<number | null>(null)
const bannedUntil = ref<string | null>(null)
const banDays = ref('3')

async function query() {
  if (!userID.value) return ElMessage.warning('请输入手机号')
  const res = await http.get<any, ApiResult>('/admin/points', { params: { userID: userID.value } })
  if (res.success && res.data) points.value = res.data.points
  else ElMessage.error(res.msg)
  const penaltyRes = await http.get<any, ApiResult>('/admin/users/penalty', { params: { userID: userID.value } })
  if (penaltyRes.success && penaltyRes.data) {
    warningCount.value = penaltyRes.data.warningCount ?? 0
    bannedUntil.value = penaltyRes.data.bannedUntil || null
  }
}

async function modify() {
  if (!userID.value) return ElMessage.warning('请先查询用户')
  const body = new URLSearchParams({
    userID: userID.value, operation: operation.value, pointAmount: pointAmount.value, reason: reason.value,
  })
  const res = await http.post<any, ApiResult>('/admin/points', body)
  if (res.success) { ElMessage.success('修改成功'); points.value = res.data?.points ?? points.value }
  else ElMessage.error(res.msg)
}

async function banUser() {
  if (!userID.value) return ElMessage.warning('请先查询用户')
  const body = new URLSearchParams({ userID: userID.value, days: banDays.value || '3' })
  const res = await http.post<any, ApiResult>('/admin/users/ban', body)
  if (res.success) { ElMessage.success(res.msg || '封禁成功'); query() }
  else ElMessage.error(res.msg)
}

async function unbanUser() {
  if (!userID.value) return ElMessage.warning('请先查询用户')
  const body = new URLSearchParams({ userID: userID.value })
  const res = await http.post<any, ApiResult>('/admin/users/unban', body)
  if (res.success) { ElMessage.success(res.msg || '解封成功'); query() }
  else ElMessage.error(res.msg)
}
</script>
