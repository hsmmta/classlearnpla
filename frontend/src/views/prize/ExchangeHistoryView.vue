<template>
  <div class="page-shell">
    <PageHeader title="积分明细" description="查看积分获取和消费历史" />

    <div class="grid gap-4 sm:grid-cols-2 mb-6">
      <div class="card p-4">
        <p class="text-sm text-brand-muted">本页获取</p>
        <p class="mt-1 text-2xl font-bold text-green-600">+{{ incomeTotal }}</p>
      </div>
      <div class="card p-4">
        <p class="text-sm text-brand-muted">本页消费</p>
        <p class="mt-1 text-2xl font-bold text-red-500">-{{ expenseTotal }}</p>
      </div>
    </div>

    <EmptyState v-if="!loading && !list.length" message="暂无积分记录" />

    <div v-else class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3" v-loading="loading">
      <div v-for="(item, i) in list" :key="i" class="card p-4 flex flex-col gap-2">
        <p class="text-sm font-medium text-gray-900 break-words">{{ item.detail }}</p>
        <div class="flex items-center justify-between text-xs text-brand-muted mt-auto pt-2 border-t border-gray-100">
          <span :class="pointValue(item.pointOP) < 0 ? 'text-red-500 font-semibold' : 'text-green-600 font-semibold'">
            {{ formatPoint(item.pointOP) }}
          </span>
          <span>{{ item.time }}</span>
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
import { computed, onMounted, ref } from 'vue'
import http from '@/api/http'
import type { ApiResult } from '@/types'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'

const list = ref<any[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const incomeTotal = computed(() => list.value.reduce((sum, item) => {
  const value = pointValue(item.pointOP)
  return value > 0 ? sum + value : sum
}, 0))
const expenseTotal = computed(() => Math.abs(list.value.reduce((sum, item) => {
  const value = pointValue(item.pointOP)
  return value < 0 ? sum + value : sum
}, 0)))

function pointValue(value: unknown) {
  return Number(String(value ?? '0').replace('+', '')) || 0
}

function formatPoint(value: unknown) {
  const n = pointValue(value)
  return n > 0 ? `+${n}` : String(n)
}

async function load() {
  loading.value = true
  const res = await http.get<any, ApiResult>('/points/history', { params: { page: page.value } })
  if (res.success && res.data) {
    list.value = (res.data as any).list || []
    total.value = (res.data as any).total || 0
    pageSize.value = (res.data as any).pageSize || 10
  }
  loading.value = false
}

onMounted(load)
</script>
