<template>
  <div class="page-shell">
    <PageHeader title="个人中心" description="管理你的个人信息" />
    <div class="grid gap-6 lg:grid-cols-[1fr_280px]">
      <div class="card p-6" v-loading="loading">
        <div class="flex items-center gap-4 mb-6 pb-5 border-b border-gray-100">
          <div class="h-14 w-14 rounded-full bg-brand-primary/10 text-brand-primary flex items-center justify-center text-xl font-bold shrink-0">
            {{ (form.userName || '?')[0] }}
          </div>
          <div>
            <h2 class="font-bold text-gray-900 text-lg">{{ form.userName }}</h2>
            <p class="text-sm text-brand-muted">积分：<strong class="text-brand-primary">{{ form.points }}</strong></p>
            <p class="text-xs text-red-500 mt-1">警告次数：{{ form.warningCount ?? 0 }}</p>
          </div>
        </div>
        <div class="mb-4 rounded-lg border border-brand-primary/20 bg-brand-primary/5 p-4">
          <div class="flex items-center justify-between gap-3">
            <div>
              <p class="text-sm font-semibold text-gray-800">每日学习打卡</p>
              <p class="text-xs text-brand-muted">每天仅可打卡一次，获得 1 积分</p>
            </div>
            <button
              class="btn-primary !px-3 !py-2 text-xs"
              :class="form.checkedInToday ? 'opacity-50 cursor-not-allowed' : ''"
              :disabled="form.checkedInToday"
              @click="checkIn"
            >
              {{ form.checkedInToday ? '今日已打卡' : '立即打卡 +1' }}
            </button>
          </div>
        </div>
        <el-form label-width="80px" @submit.prevent="save" class="space-y-1">
          <el-form-item label="昵称"><el-input v-model="form.userName" /></el-form-item>
          <el-form-item label="班级"><el-input v-model="form.classID" /></el-form-item>
          <el-form-item label="学号"><el-input v-model="form.studentID" /></el-form-item>
          <el-form-item label="性别">
            <el-select v-model="form.gender">
              <el-option label="男" value="male" />
              <el-option label="女" value="female" />
            </el-select>
          </el-form-item>
          <el-form-item label="邮箱"><el-input v-model="form.userEmail" /></el-form-item>
          <div class="flex gap-3 pt-3">
            <button type="submit" class="btn-primary"><Save class="h-4 w-4" /> 保存</button>
            <button type="button" class="btn-ghost" @click="$router.push('/profile/password')">
              <Lock class="h-4 w-4" /> 修改密码
            </button>
          </div>
        </el-form>
      </div>

      <div class="space-y-4">
        <div class="card p-4">
          <h3 class="font-semibold text-gray-800 text-sm mb-3">账号信息</h3>
          <dl class="space-y-2 text-sm">
            <div class="flex justify-between">
              <dt class="text-brand-muted">手机号</dt>
              <dd class="text-gray-700 font-medium">{{ form.userID }}</dd>
            </div>
            <div class="flex justify-between">
              <dt class="text-brand-muted">当前积分</dt>
              <dd class="text-brand-primary font-bold">{{ form.points }}</dd>
            </div>
            <div class="flex justify-between">
              <dt class="text-brand-muted">警告次数</dt>
              <dd class="text-red-500 font-bold">{{ form.warningCount ?? 0 }}</dd>
            </div>
            <div class="flex justify-between" v-if="form.bannedUntil">
              <dt class="text-brand-muted">封禁至</dt>
              <dd class="text-red-500 font-bold">{{ form.bannedUntil }}</dd>
            </div>
          </dl>
          <div class="grid gap-2 mt-4">
            <button class="btn-ghost w-full justify-center !px-3 !py-2" @click="$router.push('/points/history')">
              <History class="h-4 w-4" /> 积分明细
            </button>
            <button class="btn-ghost w-full justify-center !px-3 !py-2" @click="$router.push('/discussion/mine')">
              <MessageSquare class="h-4 w-4" /> 我的讨论
            </button>
          </div>
        </div>
        <div class="card p-4 border-red-200 ring-red-200">
          <h3 class="font-semibold text-red-600 text-sm mb-2">危险区域</h3>
          <p class="text-xs text-brand-muted mb-3">注销后数据不可恢复</p>
          <button class="inline-flex items-center gap-1.5 text-xs font-medium text-red-500 border border-red-200 rounded-md px-3 py-2 hover:bg-red-50 transition w-full justify-center" @click="cancelAccount">
            <Trash2 class="h-3.5 w-3.5" /> 注销账号
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Save, Lock, Trash2, History, MessageSquare } from '@lucide/vue'
import http from '@/api/http'
import { useUserStore } from '@/stores/user'
import type { ApiResult } from '@/types'
import PageHeader from '@/components/PageHeader.vue'

const router = useRouter()
const store = useUserStore()
const loading = ref(true)
const form = reactive<any>({})

onMounted(async () => {
  const res = await http.get<any, ApiResult>('/profile')
  if (res.success && res.data) Object.assign(form, res.data)
  loading.value = false
})

async function save() {
  const res = await http.put<any, ApiResult>('/profile', null, {
    params: {
      userName: form.userName ?? '',
      classID: form.classID ?? '',
      studentID: form.studentID ?? '',
      gender: form.gender ?? '',
      userEmail: form.userEmail ?? '',
    },
  })
  if (res.success) { store.setUser(res.data as any); ElMessage.success('已保存') }
  else ElMessage.error(res.msg)
}

async function checkIn() {
  const res = await http.post<any, ApiResult<{ points: number; checkedInToday: boolean }>>('/profile/check-in')
  if (res.success) {
    form.points = res.data?.points ?? form.points
    form.checkedInToday = true
    ElMessage.success('打卡成功，积分 +1')
  } else ElMessage.error(res.msg)
}

async function cancelAccount() {
  const { value } = await ElMessageBox.prompt('请输入密码确认注销', '注销账号', { inputType: 'password', type: 'warning' })
  const res = await http.delete<any, ApiResult>('/profile', { params: { password: value } })
  if (res.success) { await store.logout(); router.push('/login') }
}
</script>
