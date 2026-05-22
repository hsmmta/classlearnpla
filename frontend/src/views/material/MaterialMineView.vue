<template>
  <div class="page-shell">
    <PageHeader title="我的资料" description="管理你上传的全部资料">
      <button class="btn-primary" @click="$router.push('/materials/new')">
        <Upload class="h-4 w-4" /> 上传
      </button>
    </PageHeader>

    <EmptyState v-if="!loading && !list.length" message="还没有上传任何资料">
      <button class="btn-primary mt-2" @click="$router.push('/materials/new')">立即上传</button>
    </EmptyState>

    <div v-else class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3" v-loading="loading">
      <div v-for="m in list" :key="m.materialID" class="card p-4 flex flex-col gap-3">
        <div class="flex items-start justify-between gap-2">
          <h3 class="font-medium text-gray-900 text-sm break-words line-clamp-2">{{ m.materialTitle }}</h3>
          <span class="label-tag shrink-0" :class="{
            '!bg-green-100 !text-green-700': m.materialState === 'approved',
            '!bg-yellow-100 !text-yellow-700': m.materialState === 'pending',
            '!bg-red-100 !text-red-700': m.materialState === 'rejected',
          }">{{ stateLabel(m.materialState) }}</span>
        </div>
        <div class="flex items-center gap-2 mt-auto pt-2 border-t border-gray-100">
          <button class="btn-ghost !px-3 !py-1.5 text-xs" @click="$router.push(`/materials/${m.materialID}`)">
            查看讨论
          </button>
          <button class="btn-ghost !px-3 !py-1.5 text-xs" @click="$router.push(`/materials/${m.materialID}/edit`)">
            <Pencil class="h-3.5 w-3.5" /> 编辑
          </button>
          <button class="inline-flex items-center gap-1.5 rounded-md border border-red-200 bg-white px-3 py-1.5 text-xs font-medium text-red-500 hover:bg-red-50 transition" @click="remove(m.materialID)">
            <Trash2 class="h-3.5 w-3.5" /> 删除
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Upload, Pencil, Trash2 } from '@lucide/vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'
import type { ApiResult } from '@/types'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'

const list = ref<any[]>([])
const loading = ref(false)

const stateLabel = (s: string) => ({ approved: '已通过', pending: '审核中', rejected: '已驳回' }[s] ?? s)

async function load() {
  loading.value = true
  const res = await http.get<any, ApiResult>('/materials/mine')
  if (res.success) list.value = res.data || []
  loading.value = false
}

async function remove(id: string) {
  await ElMessageBox.confirm('确定删除这份资料？', '删除确认', { type: 'warning' })
  const res = await http.delete<any, ApiResult>(`/materials/${id}`)
  res.success ? (ElMessage.success('已删除'), load()) : ElMessage.error(res.msg)
}
onMounted(load)
</script>
