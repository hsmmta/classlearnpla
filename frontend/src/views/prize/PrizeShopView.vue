<template>
  <div class="page-shell">
    <PageHeader title="积分商城" :description="`我的积分：${userPoints}`">
      <button class="btn-ghost" @click="$router.push('/points/history')">
        <History class="h-4 w-4" /> 积分明细
      </button>
    </PageHeader>

    <EmptyState v-if="!loading && !goods.length" message="暂时没有可兑换的商品" />

    <div v-else class="grid gap-3 sm:gap-4 grid-cols-2 lg:grid-cols-4" v-loading="loading">
      <div v-for="g in goods" :key="g.goodsID" class="card flex flex-col">
        <div class="flex-1 p-4">
          <div class="flex items-start justify-between gap-2 mb-2">
            <h3 class="font-semibold text-gray-900 text-sm break-words line-clamp-2">{{ g.goodsName }}</h3>
            <span class="label-tag shrink-0">{{ g.goodsType }}</span>
          </div>
          <p class="text-xs text-brand-muted mt-1">库存：{{ g.currentNum }}</p>
        </div>
        <div class="px-4 pb-4 pt-2 border-t border-gray-100 flex items-center justify-between">
          <span class="font-bold text-brand-primary text-base">{{ g.needPoints }} <span class="text-xs font-normal text-brand-muted">积分</span></span>
          <button
            class="btn-primary !px-3 !py-1.5 text-xs"
            :disabled="userPoints < g.needPoints || g.currentNum <= 0"
            :class="userPoints < g.needPoints || g.currentNum <= 0 ? 'opacity-50 cursor-not-allowed' : ''"
            @click="exchange(g)"
          >兑换</button>
        </div>
      </div>
    </div>

    <el-pagination
      v-if="total > pageSize"
      class="mt-6 flex justify-center"
      layout="prev, pager, next"
      :total="total"
      :page-size="pageSize"
      v-model:current-page="page"
      @current-change="load"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { History } from '@lucide/vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'
import type { ApiResult } from '@/types'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'

const goods = ref<any[]>([])
const userPoints = ref(0)
const page = ref(1)
const pageSize = 12
const total = ref(0)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await http.get<any, ApiResult>('/prizes', { params: { page: page.value } })
    if (res.success && res.data) {
      goods.value = res.data.goods || []
      userPoints.value = res.data.userPoints || 0
      total.value = res.data.total || 0
    }
  } finally { loading.value = false }
}

async function exchange(g: any) {
  await ElMessageBox.confirm(`使用 ${g.needPoints} 积分兑换「${g.goodsName}」？`, '确认兑换', { type: 'info' })
  const body = new URLSearchParams({ itemID: g.goodsID, needPoints: String(g.needPoints) })
  const res = await http.post<any, ApiResult>('/prizes/exchange', body)
  res.success ? (ElMessage.success(res.msg), load()) : ElMessage.error(res.msg)
}
onMounted(load)
</script>
