<template>
  <div class="page-shell">
    <PageHeader title="我的讨论" description="查看我发起的讨论和审核状态">
      <button class="btn-primary" @click="$router.push('/discussion/ask')">
        <Plus class="h-4 w-4" /> 发起讨论
      </button>
    </PageHeader>

    <EmptyState v-if="!loading && !list.length" message="你还没有发起过讨论" />

    <div v-else class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3" v-loading="loading">
      <component
        v-for="q in list"
        :key="q.questionID"
        :is="q.questionState === '审核通过' ? 'router-link' : 'div'"
        :to="q.questionState === '审核通过' ? `/discussion/${q.questionID}` : undefined"
        class="card p-4 flex flex-col gap-3"
        :class="q.questionState === '审核通过' ? 'cursor-pointer hover:ring-brand-primary/40' : ''"
      >
        <div class="flex items-start justify-between gap-2">
          <h3 class="font-medium text-gray-900 text-sm break-words line-clamp-2">{{ q.questionTitle }}</h3>
          <span class="label-tag shrink-0" :class="stateClass(q.questionState)">
            {{ stateLabel(q.questionState) }}
          </span>
        </div>
        <p class="text-xs text-brand-muted line-clamp-3">{{ q.questionContent }}</p>
        <div class="flex items-center justify-between pt-2 border-t border-gray-100 mt-auto text-xs text-gray-400">
          <span>{{ q.creationTime }}</span>
          <div class="flex items-center gap-2">
            <span v-if="q.bestAnswerID" class="text-green-600 font-medium">已采纳答案</span>
            <button
              class="inline-flex items-center gap-1 rounded border border-red-200 px-2 py-1 text-red-500 hover:bg-red-50 transition"
              @click.stop.prevent="removeQuestion(q.questionID)"
            >
              <Trash2 class="h-3.5 w-3.5" /> 删除
            </button>
          </div>
        </div>
      </component>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Plus, Trash2 } from '@lucide/vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'
import type { ApiResult } from '@/types'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'

const list = ref<any[]>([])
const loading = ref(false)

function stateLabel(state: string) {
  return ({ 审核通过: '已通过', 待审核: '审核中', 审核拒绝: '已驳回' } as Record<string, string>)[state] ?? state
}

function stateClass(state: string) {
  if (state === '审核通过') return '!bg-green-100 !text-green-700'
  if (state === '待审核') return '!bg-yellow-100 !text-yellow-700'
  if (state === '审核拒绝') return '!bg-red-100 !text-red-700'
  return ''
}

onMounted(async () => {
  await load()
})

async function load() {
  loading.value = true
  const res = await http.get<any, ApiResult>('/questions/mine')
  if (res.success) list.value = res.data || []
  loading.value = false
}

async function removeQuestion(questionID: string) {
  try {
    await ElMessageBox.confirm('确定删除这条帖子吗？删除后不可恢复。', '删除确认', { type: 'warning' })
    const res = await http.delete<any, ApiResult>(`/questions/${questionID}`)
    if (res.success) {
      ElMessage.success(res.msg || '已删除')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  } catch {
    // User canceled deletion.
  }
}
</script>
