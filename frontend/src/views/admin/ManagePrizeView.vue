<template>
  <div class="page-shell">
    <PageHeader title="奖品管理" description="上架、库存维护、上下架与兑换申请处理" />

    <!-- Add form -->
    <div class="card p-4 sm:p-5 mb-6">
      <h3 class="font-semibold text-gray-800 text-sm mb-4">上架新商品</h3>
      <el-form @submit.prevent="add" class="grid gap-2 sm:gap-3 sm:grid-cols-2 lg:grid-cols-3">
        <el-form-item label="ID"><el-input v-model="form.goodsID" placeholder="商品ID" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.goodsName" placeholder="商品名称" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.goodsType" class="!w-full">
            <el-option label="实体奖品" value="实体奖品" />
            <el-option label="虚拟奖品" value="虚拟奖品" />
          </el-select>
        </el-form-item>
        <el-form-item label="积分"><el-input v-model="form.needPoints" type="number" /></el-form-item>
        <el-form-item label="库存"><el-input v-model="form.currentNum" type="number" /></el-form-item>
        <el-form-item class="sm:col-span-2 lg:col-span-3 !mb-0">
          <button type="submit" class="btn-primary w-full sm:w-auto justify-center">
            <Plus class="h-4 w-4" /> 上架
          </button>
        </el-form-item>
      </el-form>
    </div>

    <EmptyState v-if="!loading && !list.length" message="暂无商品" />

    <div v-else class="grid gap-3 sm:gap-4 grid-cols-2 lg:grid-cols-4" v-loading="loading">
      <div v-for="g in list" :key="g.goodsID" class="card p-4 flex flex-col gap-2">
        <div class="flex items-start justify-between gap-2">
          <h3 class="font-semibold text-gray-900 text-sm break-words">{{ g.goodsName }}</h3>
          <span class="label-tag shrink-0">{{ g.goodsType }}</span>
        </div>
        <p class="text-xs text-brand-muted">ID: {{ g.goodsID }}</p>
        <p class="text-xs" :class="g.enabled === 1 ? 'text-green-600' : 'text-red-500'">
          状态：{{ g.enabled === 1 ? '上架中' : '已下架' }}
        </p>
        <div class="mt-auto pt-2 border-t border-gray-100 space-y-2">
          <div class="text-xs text-gray-700">
            <span class="font-semibold text-brand-primary">{{ g.needPoints }}</span> 积分 ·
            <span v-if="g.goodsType === '实体奖品'">库存 {{ g.currentNum }}</span>
            <span v-else>虚拟奖品不限量</span>
          </div>
          <div v-if="g.goodsType === '实体奖品'" class="flex items-center gap-2">
            <el-input v-model="stockDelta[g.goodsID]" type="number" class="!w-20" placeholder="+10" />
            <button class="btn-ghost !px-2 !py-1 text-xs" @click="adjustStock(g.goodsID)">增减库存</button>
          </div>
          <div class="flex items-center gap-2">
            <button
              class="inline-flex items-center gap-1 text-xs border rounded px-2 py-1 transition"
              :class="g.enabled === 1 ? 'text-orange-600 border-orange-200 hover:bg-orange-50' : 'text-green-600 border-green-200 hover:bg-green-50'"
              @click="toggle(g)"
            >
              {{ g.enabled === 1 ? '下架' : '上架' }}
            </button>
            <button class="inline-flex items-center gap-1 text-xs text-red-500 border border-red-200 rounded px-2 py-1 hover:bg-red-50 transition" @click="remove(g.goodsID)">
            <Trash2 class="h-3 w-3" /> 删除
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="card p-4 sm:p-5 mt-6">
      <div class="flex items-center justify-between mb-3">
        <h3 class="font-semibold text-gray-800 text-sm">实体奖品兑换申请</h3>
        <button class="btn-ghost !px-3 !py-1.5 text-xs" @click="loadRequests">刷新</button>
      </div>
      <EmptyState v-if="!requestList.length" message="暂无兑换申请" />
      <div v-else class="space-y-3">
        <div v-for="r in requestList" :key="r.requestID" class="rounded-lg border border-gray-200 p-3">
          <div class="flex items-start justify-between gap-2">
            <div class="text-sm">
              <p class="font-semibold text-gray-900">{{ r.goodsName }}</p>
              <p class="text-xs text-brand-muted mt-1">
                申请ID: {{ r.requestID }} · 用户: {{ r.userName || r.userID }}（{{ r.userID }}）
              </p>
              <p class="text-xs text-brand-muted mt-1">申请时间：{{ r.createdAt }}</p>
              <p class="text-xs mt-1" :class="requestStatusClass(r.status)">状态：{{ requestStatusText(r.status) }}</p>
              <p v-if="r.remark" class="text-xs text-gray-600 mt-1">备注：{{ r.remark }}</p>
            </div>
            <div v-if="r.status === 'pending'" class="flex items-center gap-2">
              <button class="btn-primary !px-3 !py-1.5 text-xs" @click="processRequest(r, 'fulfilled')">标记已兑换</button>
              <button class="btn-ghost !px-3 !py-1.5 text-xs text-red-500" @click="processRequest(r, 'rejected')">拒绝</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Plus, Trash2 } from '@lucide/vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'
import type { ApiResult } from '@/types'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'

const list = ref<any[]>([])
const loading = ref(false)
const form = reactive({ goodsID: '', goodsName: '', goodsType: '实体奖品', needPoints: '0', currentNum: '0' })
const stockDelta = reactive<Record<string, string>>({})
const requestList = ref<any[]>([])

async function load() {
  loading.value = true
  const res = await http.get<any, ApiResult>('/admin/prizes')
  if (res.success) list.value = res.data || []
  loading.value = false
}

async function add() {
  const body = new URLSearchParams(form as any)
  const res = await http.post<any, ApiResult>('/admin/prizes', body)
  res.success ? (ElMessage.success('上架成功'), load()) : ElMessage.error(res.msg)
}

async function remove(id: string) {
  const res = await http.delete<any, ApiResult>('/admin/prizes', { params: { goodsID: id } })
  res.success ? (ElMessage.success('已删除'), load()) : ElMessage.error(res.msg)
}

async function adjustStock(goodsID: string) {
  const delta = Number(stockDelta[goodsID] || '0')
  if (!Number.isFinite(delta) || delta === 0) {
    ElMessage.warning('请输入非 0 的库存变更值')
    return
  }
  const body = new URLSearchParams({ goodsID, delta: String(delta) })
  const res = await http.post<any, ApiResult>('/admin/prizes/stock', body)
  if (res.success) {
    ElMessage.success('库存已更新')
    stockDelta[goodsID] = ''
    load()
  } else {
    ElMessage.error(res.msg)
  }
}

async function toggle(g: any) {
  const next = g.enabled === 1 ? 0 : 1
  const body = new URLSearchParams({ goodsID: g.goodsID, enabled: String(next) })
  const res = await http.post<any, ApiResult>('/admin/prizes/toggle', body)
  if (res.success) {
    ElMessage.success(res.msg || '状态已更新')
    load()
  } else {
    ElMessage.error(res.msg)
  }
}

async function loadRequests() {
  const res = await http.get<any, ApiResult>('/admin/exchange-requests', { params: { limit: 200 } })
  if (res.success) requestList.value = res.data || []
  else ElMessage.error(res.msg)
}

async function processRequest(r: any, action: 'fulfilled' | 'rejected') {
  const body = new URLSearchParams({ requestID: String(r.requestID), action })
  const res = await http.post<any, ApiResult>('/admin/exchange-requests/process', body)
  if (res.success) {
    ElMessage.success(action === 'fulfilled' ? '已标记兑换完成' : '已拒绝申请')
    loadRequests()
  } else {
    ElMessage.error(res.msg)
  }
}

function requestStatusText(status: string) {
  if (status === 'pending') return '待处理'
  if (status === 'fulfilled') return '已兑换'
  if (status === 'rejected') return '已拒绝'
  return status
}

function requestStatusClass(status: string) {
  if (status === 'pending') return 'text-amber-600'
  if (status === 'fulfilled') return 'text-green-600'
  if (status === 'rejected') return 'text-red-500'
  return 'text-brand-muted'
}

onMounted(async () => {
  await load()
  await loadRequests()
})
</script>
