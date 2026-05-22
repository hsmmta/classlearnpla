<template>
  <div class="page-shell">
    <PageHeader title="资料区" description="搜索并浏览同学上传的学习资料">
      <button class="btn-primary" @click="$router.push('/materials/new')">
        <Upload class="h-4 w-4" /> 上传资料
      </button>
    </PageHeader>

    <!-- Search -->
    <div class="flex gap-2 mb-6">
      <el-input v-model="q" placeholder="搜索资料标题、科目…" clearable @keyup.enter="load" class="flex-1" />
      <button class="btn-ghost" @click="load">
        <Search class="h-4 w-4" /> 搜索
      </button>
    </div>

    <div v-if="loading" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <div v-for="i in 6" :key="i" class="card p-4 animate-pulse">
        <div class="h-4 bg-gray-200 rounded w-3/4 mb-3" />
        <div class="h-3 bg-gray-100 rounded w-1/2" />
      </div>
    </div>

    <EmptyState v-else-if="!list.length" message="暂无资料，快来上传第一份吧">
      <button class="btn-primary mt-2" @click="$router.push('/materials/new')">上传资料</button>
    </EmptyState>

    <div v-else class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <DataCard
        v-for="m in list"
        :key="m.materialID"
        :title="m.materialTitle"
        :subtitle="m.uploaderName + ' · ' + (m.uploadTime || '')"
        :tag="m.materialSubject"
        :to="`/materials/${m.materialID}`"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Search, Upload } from '@lucide/vue'
import http from '@/api/http'
import type { ApiResult } from '@/types'
import PageHeader from '@/components/PageHeader.vue'
import DataCard from '@/components/DataCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const q = ref('')
const list = ref<any[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await http.get<any, ApiResult<any[]>>('/materials', { params: { q: q.value } })
    if (res.success) list.value = res.data || []
  } finally { loading.value = false }
}
onMounted(load)
</script>
