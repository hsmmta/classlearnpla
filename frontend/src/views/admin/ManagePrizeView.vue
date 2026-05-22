<template>
  <div class="page-shell">
    <PageHeader title="奖品管理" description="上架或删除积分商城商品" />

    <!-- Add form -->
    <div class="card p-5 mb-6">
      <h3 class="font-semibold text-gray-800 text-sm mb-4">上架新商品</h3>
      <el-form inline @submit.prevent="add" class="flex flex-wrap gap-y-2">
        <el-form-item label="ID"><el-input v-model="form.goodsID" placeholder="商品ID" class="!w-28" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.goodsName" placeholder="商品名称" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.goodsType" class="!w-28">
            <el-option label="实体奖品" value="实体奖品" />
            <el-option label="虚拟奖品" value="虚拟奖品" />
          </el-select>
        </el-form-item>
        <el-form-item label="积分"><el-input v-model="form.needPoints" type="number" class="!w-24" /></el-form-item>
        <el-form-item label="库存"><el-input v-model="form.currentNum" type="number" class="!w-24" /></el-form-item>
        <el-form-item>
          <button type="submit" class="btn-primary">
            <Plus class="h-4 w-4" /> 上架
          </button>
        </el-form-item>
      </el-form>
    </div>

    <EmptyState v-if="!loading && !list.length" message="暂无商品" />

    <div v-else class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4" v-loading="loading">
      <div v-for="g in list" :key="g.goodsID" class="card p-4 flex flex-col gap-2">
        <div class="flex items-start justify-between gap-2">
          <h3 class="font-semibold text-gray-900 text-sm break-words">{{ g.goodsName }}</h3>
          <span class="label-tag shrink-0">{{ g.goodsType }}</span>
        </div>
        <p class="text-xs text-brand-muted">ID: {{ g.goodsID }}</p>
        <div class="flex items-center justify-between mt-auto pt-2 border-t border-gray-100">
          <div class="text-xs text-gray-700">
            <span class="font-semibold text-brand-primary">{{ g.needPoints }}</span> 积分 · 库存 {{ g.currentNum }}
          </div>
          <button class="inline-flex items-center gap-1 text-xs text-red-500 border border-red-200 rounded px-2 py-1 hover:bg-red-50 transition" @click="remove(g.goodsID)">
            <Trash2 class="h-3 w-3" /> 删除
          </button>
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
onMounted(load)
</script>
