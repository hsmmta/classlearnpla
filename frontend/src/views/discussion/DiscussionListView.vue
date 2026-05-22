<template>
  <div class="page-shell">
    <PageHeader title="讨论区" description="提问或回答，互帮互助">
      <button class="btn-ghost" @click="$router.push('/discussion/mine')">
        <MessageSquare class="h-4 w-4" /> 我的讨论
      </button>
      <button class="btn-primary" @click="$router.push('/discussion/ask')">
        <Plus class="h-4 w-4" /> 提问
      </button>
    </PageHeader>

    <EmptyState v-if="!loading && !list.length" message="还没有人提问，快来第一个吧！" />

    <div v-else class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3" v-loading="loading">
      <router-link
        v-for="q in list"
        :key="q.questionID"
        :to="`/discussion/${q.questionID}`"
        class="card overflow-hidden"
      >
        <img
          v-if="q.firstImageUrl"
          :src="q.firstImageUrl"
          class="h-36 w-full object-cover"
          alt="question cover"
        />
        <div class="p-4">
          <h3 class="font-medium text-sm text-gray-900 line-clamp-2">{{ q.questionTitle }}</h3>
          <p class="mt-1 text-xs text-brand-muted line-clamp-3">{{ q.questionContent }}</p>
          <p class="mt-3 text-xs text-gray-400">{{ q.userName }} · {{ q.creationTime }}</p>
        </div>
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Plus, MessageSquare } from '@lucide/vue'
import http from '@/api/http'
import type { ApiResult } from '@/types'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'

const list = ref<any[]>([])
const loading = ref(false)
onMounted(async () => {
  loading.value = true
  const res = await http.get<any, ApiResult>('/questions')
  if (res.success) list.value = res.data || []
  loading.value = false
})
</script>
