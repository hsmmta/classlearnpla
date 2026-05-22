<template>
  <div class="page-shell" v-loading="loading">
    <div v-if="question" class="space-y-5">
      <!-- Question card -->
      <div class="card p-5">
        <h1 class="section-title break-words">{{ question.questionTitle }}</h1>
        <p class="mt-1 text-sm text-brand-muted">{{ question.userName }} · {{ question.creationTime }}</p>
        <p class="mt-4 text-gray-700 whitespace-pre-wrap leading-relaxed">{{ question.questionContent }}</p>
        <div v-if="question.imageUrls?.length" class="mt-4 grid gap-3 sm:grid-cols-2">
          <a
            v-for="(url, idx) in question.imageUrls"
            :key="url + idx"
            :href="url"
            target="_blank"
            rel="noopener noreferrer"
            class="block rounded-lg overflow-hidden ring-1 ring-gray-200"
          >
            <img :src="url" class="w-full h-56 object-cover" alt="question image" />
          </a>
        </div>
      </div>

      <!-- Answers -->
      <div class="card p-5">
        <h2 class="font-semibold text-gray-900 mb-4">{{ comments.length }} 条回答</h2>
        <EmptyState v-if="!comments.length" message="暂无回答，快来第一个回答吧！" />
        <div v-else class="space-y-4">
          <div
            v-for="c in topLevelComments"
            :key="c.commentID"
            class="rounded-lg p-4 border"
            :class="c.isBestAnswer ? 'border-green-300 bg-green-50' : 'border-gray-100'"
          >
            <div class="flex items-center justify-between gap-2 mb-2">
              <div class="flex items-center gap-2">
                <span class="font-medium text-sm text-gray-800">{{ c.userName }}</span>
                <span class="text-xs text-brand-muted">{{ formatTime(c.commentTime) }}</span>
              </div>
              <span v-if="c.isBestAnswer" class="inline-flex items-center rounded-full bg-green-100 px-2 py-0.5 text-xs font-medium text-green-700">
                <Star class="h-3 w-3 mr-1" /> 最满意
              </span>
            </div>
            <p class="text-sm whitespace-pre-wrap" :class="c.deleted ? 'text-gray-400 italic' : 'text-gray-700'">{{ c.commentContent }}</p>
            <div class="flex items-center gap-3 mt-3">
              <button
                v-if="!c.deleted"
                class="inline-flex items-center gap-1 text-xs text-brand-muted hover:text-brand-primary transition"
                @click="like(c.commentID)"
              >
                <ThumbsUp class="h-3.5 w-3.5" /> {{ c.likes }}
              </button>
              <button
                v-if="!c.deleted"
                class="inline-flex items-center gap-1 text-xs text-brand-muted hover:text-brand-primary transition"
                @click="startReply(c)"
              >
                <MessageSquare class="h-3.5 w-3.5" /> 回复
              </button>
              <button
                v-if="!c.deleted && canSetBest && !question.bestAnswerID && c.userID !== store.user?.userID"
                class="inline-flex items-center gap-1 text-xs text-brand-muted hover:text-green-600 transition"
                @click="setBest(c.commentID)"
              >
                <Star class="h-3.5 w-3.5" /> 设为最满意
              </button>
              <button
                v-if="!c.deleted && c.userID === store.user?.userID"
                class="inline-flex items-center gap-1 text-xs text-red-500 hover:text-red-600 transition"
                @click="removeComment(c.commentID)"
              >
                <Trash2 class="h-3.5 w-3.5" /> 删除
              </button>
            </div>

            <div v-if="childComments(c.commentID).length" class="mt-4 space-y-2 border-l-2 border-gray-100 pl-3">
              <div v-for="child in childComments(c.commentID)" :key="child.commentID" class="rounded-md bg-gray-50 p-3">
                <div class="flex items-center justify-between gap-2 mb-1">
                  <div class="flex items-center gap-2 text-xs">
                    <span class="font-medium text-gray-800">{{ child.userName }}</span>
                    <span class="text-brand-muted">{{ formatTime(child.commentTime) }}</span>
                  </div>
                  <div class="flex items-center gap-2">
                    <button
                      v-if="!child.deleted"
                      class="inline-flex items-center gap-1 text-xs text-brand-muted hover:text-brand-primary transition"
                      @click="startReply(child)"
                    >
                      <MessageSquare class="h-3.5 w-3.5" /> 回复
                    </button>
                    <button
                      v-if="!child.deleted && child.userID === store.user?.userID"
                      class="inline-flex items-center gap-1 text-xs text-red-500 hover:text-red-600 transition"
                      @click="removeComment(child.commentID)"
                    >
                      <Trash2 class="h-3.5 w-3.5" /> 删除
                    </button>
                  </div>
                </div>
                <p class="text-sm whitespace-pre-wrap" :class="child.deleted ? 'text-gray-400 italic' : 'text-gray-700'">
                  <span v-if="child.replyToUserName && !child.deleted" class="text-brand-primary mr-1">@{{ child.replyToUserName }}</span>
                  {{ child.commentContent }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- Reply box -->
        <div class="mt-5 flex flex-col gap-2">
          <div v-if="replyingTo" class="rounded-md border border-brand-primary/20 bg-brand-primary/5 px-3 py-2 text-xs text-brand-primary">
            正在回复 @{{ replyingTo.userName }}
            <button class="ml-2 underline" @click="cancelReply">取消</button>
          </div>
          <el-input v-model="replyText" type="textarea" :rows="3" :placeholder="replyingTo ? `回复 @${replyingTo.userName}…` : '写下你的回答…'" />
          <button class="btn-primary self-end" :disabled="posting" @click="reply">
            <Send class="h-4 w-4" /> 提交回答
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MessageSquare, Send, Star, ThumbsUp, Trash2 } from '@lucide/vue'
import http from '@/api/http'
import { useUserStore } from '@/stores/user'
import type { ApiResult } from '@/types'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const store = useUserStore()
const loading = ref(true)
const question = ref<any>(null)
const comments = ref<any[]>([])
const replyText = ref('')
const posting = ref(false)
const replyingTo = ref<any | null>(null)

const canSetBest = computed(() => store.user?.userID === question.value?.userID)
const topLevelComments = computed(() => comments.value.filter((c) => !toNum(c.parentCommentID)))

async function load() {
  try {
    const res = await http.get<any, ApiResult>(`/questions/${route.params.id}`)
    if (res.success && res.data) {
      question.value = res.data.question
      comments.value = res.data.comments || []
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载问题失败')
  } finally {
    loading.value = false
  }
}

async function reply() {
  const content = replyText.value.trim()
  if (!content) {
    ElMessage.warning('请输入回复内容')
    return
  }
  posting.value = true
  const body = new URLSearchParams({ commentContent: content })
  if (replyingTo.value) {
    body.set('parentCommentID', String(rootCommentId(replyingTo.value)))
    body.set('replyToUserID', replyingTo.value.userID)
  }
  try {
    const res = await http.post<any, ApiResult>(`/questions/${route.params.id}/comments`, body)
    if (res.success) {
      replyText.value = ''
      replyingTo.value = null
      ElMessage.success('回复成功')
      await load()
    } else {
      ElMessage.error(res.msg || '回复失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '回复失败')
  } finally {
    posting.value = false
  }
}

async function like(cid: number) {
  const res = await http.post<any, ApiResult>(`/questions/${route.params.id}/comments/${cid}/like`)
  if (res.success) load()
  else ElMessage.error(res.msg)
}

async function setBest(cid: number) {
  const body = new URLSearchParams({ commentID: String(cid) })
  const res = await http.post<any, ApiResult>(`/questions/${route.params.id}/best-answer`, body)
  res.success ? (ElMessage.success('设置成功'), load()) : ElMessage.error(res.msg)
}

function childComments(parentId: number) {
  return comments.value.filter((c) => toNum(c.parentCommentID) === parentId)
}

function toNum(value: unknown) {
  if (value === null || value === undefined || value === '') return 0
  const n = Number(value)
  return Number.isNaN(n) ? 0 : n
}

function rootCommentId(comment: any) {
  const parent = toNum(comment.parentCommentID)
  return parent > 0 ? parent : Number(comment.commentID)
}

function startReply(comment: any) {
  replyingTo.value = comment
}

function cancelReply() {
  replyingTo.value = null
}

async function removeComment(commentId: number) {
  try {
    await ElMessageBox.confirm('确定删除这条评论吗？', '删除确认', { type: 'warning' })
    const res = await http.delete<any, ApiResult>(`/questions/${route.params.id}/comments/${commentId}`)
    if (res.success) {
      ElMessage.success(res.msg || '评论已删除')
      await load()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch {
    // cancel
  }
}

function formatTime(value: string | null | undefined) {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  return d.toLocaleString('zh-CN', { hour12: false })
}
onMounted(load)
</script>
