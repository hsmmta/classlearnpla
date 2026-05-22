<template>
  <div class="page-shell">
    <PageHeader title="问题审核" description="待审核的讨论问题" />
    <div class="mb-4 inline-flex rounded-lg border border-gray-200 bg-white p-1">
      <button class="px-3 py-1.5 text-sm rounded-md transition" :class="tab === 'pending' ? 'bg-brand-primary text-white' : 'text-gray-600'" @click="switchTab('pending')">待审核</button>
      <button class="px-3 py-1.5 text-sm rounded-md transition" :class="tab === 'approved' ? 'bg-brand-primary text-white' : 'text-gray-600'" @click="switchTab('approved')">已通过</button>
    </div>

    <EmptyState v-if="!loading && !list.length" message="暂无待审核问题" />

    <div v-else class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3" v-loading="loading">
      <div v-for="q in list" :key="q.questionID" class="card p-4 flex flex-col gap-3">
        <div>
          <h3 class="font-medium text-gray-900 text-sm break-words line-clamp-2">{{ q.questionTitle }}</h3>
          <p class="text-xs text-brand-muted mt-1">{{ q.creationTime }}</p>
          <p class="mt-2 text-sm text-gray-700 whitespace-pre-wrap break-words line-clamp-6">
            {{ q.questionContent || '（无正文）' }}
          </p>
          <div v-if="q.imageUrls?.length" class="mt-3 grid grid-cols-3 gap-2">
            <a
              v-for="(url, i) in q.imageUrls"
              :key="url + i"
              :href="url"
              target="_blank"
              rel="noopener noreferrer"
              class="rounded-md overflow-hidden ring-1 ring-gray-200"
            >
              <img :src="url" alt="question image" class="h-20 w-full object-cover" />
            </a>
          </div>
        </div>
        <div class="flex gap-2 pt-2 border-t border-gray-100 mt-auto">
          <button v-if="tab === 'pending'" class="flex-1 inline-flex items-center justify-center gap-1.5 rounded-md bg-green-50 border border-green-200 px-3 py-2 text-xs font-medium text-green-700 hover:bg-green-100 transition" @click="audit(q.questionID, 'approve')">
            <CheckCircle class="h-3.5 w-3.5" /> 通过
          </button>
          <button v-if="tab === 'pending'" class="flex-1 inline-flex items-center justify-center gap-1.5 rounded-md bg-red-50 border border-red-200 px-3 py-2 text-xs font-medium text-red-600 hover:bg-red-100 transition" @click="audit(q.questionID, 'reject')">
            <XCircle class="h-3.5 w-3.5" /> 驳回
          </button>
          <button v-else class="flex-1 inline-flex items-center justify-center gap-1.5 rounded-md bg-red-50 border border-red-200 px-3 py-2 text-xs font-medium text-red-600 hover:bg-red-100 transition" @click="adminDelete(q.questionID)">
            <XCircle class="h-3.5 w-3.5" /> 删除帖子
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { CheckCircle, XCircle } from '@lucide/vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'
import type { ApiResult } from '@/types'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'

const list = ref<any[]>([])
const loading = ref(false)
const tab = ref<'pending' | 'approved'>('pending')

async function load() {
  loading.value = true
  const endpoint = tab.value === 'pending' ? '/questions/pending' : '/questions/approved'
  const res = await http.get<any, ApiResult>(endpoint)
  if (res.success) list.value = res.data || []
  loading.value = false
}

function switchTab(next: 'pending' | 'approved') {
  tab.value = next
  load()
}

async function audit(id: string, action: string) {
  const body = new URLSearchParams({ action })
  const res = await http.post<any, ApiResult>(`/questions/${id}/audit`, body)
  res.success ? (ElMessage.success('操作完成'), load()) : ElMessage.error(res.msg)
}
async function adminDelete(id: string) {
  const res = await http.post<any, ApiResult>(`/questions/${id}/admin-delete`)
  res.success ? (ElMessage.success('删除成功'), load()) : ElMessage.error(res.msg)
}
onMounted(load)
</script>
