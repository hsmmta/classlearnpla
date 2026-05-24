<template>
  <div class="page-shell" v-loading="loading">
    <div v-if="material" class="grid gap-6 lg:grid-cols-[1fr_300px]">
      <!-- Main content -->
      <div class="space-y-5">
        <div class="card p-5">
          <h1 class="section-title break-words">{{ material.materialTitle }}</h1>
          <p class="mt-2 text-sm text-brand-muted">
            {{ material.materialSubject }} · {{ material.uploaderName }}
          </p>
          <div class="mt-4">
            <button v-if="material.materialType === 'pdf' && material.filePath" class="btn-primary" @click="openPdf">
              <FileText class="h-4 w-4" /> 预览 PDF
            </button>
            <div v-else class="prose prose-sm max-w-none text-gray-700 leading-relaxed whitespace-pre-wrap">
              {{ material.materialContent }}
            </div>
          </div>
        </div>

        <!-- Discussion -->
        <div class="card p-5">
          <div class="mb-4 flex items-center justify-between gap-3">
            <h2 class="font-semibold text-gray-900">讨论区</h2>
            <span class="label-tag">{{ comments.length }} 条讨论</span>
          </div>
          <EmptyState v-if="!comments.length" message="暂无讨论，快来发表第一条吧" />
          <div v-else class="space-y-3">
            <div v-for="c in topLevelComments" :key="c.commentID" class="pb-3 border-b border-gray-100 last:border-0">
              <div class="flex gap-3">
                <div class="h-8 w-8 rounded-full bg-brand-primary/10 text-brand-primary flex items-center justify-center text-sm font-semibold shrink-0">
                  {{ (c.userName || '?')[0] }}
                </div>
                <div class="min-w-0 flex-1">
                  <div class="flex items-center justify-between gap-2">
                    <span class="text-sm font-medium text-gray-800">{{ c.userName }}</span>
                    <span class="text-xs text-brand-muted">{{ formatTime(c.commentTime) }}</span>
                  </div>
                  <p class="mt-0.5 break-words text-sm whitespace-pre-wrap" :class="c.deleted ? 'text-gray-400 italic' : 'text-gray-600'">
                    {{ c.commentContent }}
                  </p>
                  <div class="mt-2 flex items-center gap-3">
                    <button
                      v-if="!c.deleted && !isAdmin"
                      class="inline-flex items-center gap-1 text-xs text-brand-muted hover:text-brand-primary transition"
                      @click="startReply(c)"
                    >
                      <MessageSquare class="h-3.5 w-3.5" /> 回复
                    </button>
                    <button
                      v-if="!c.deleted && isAdmin"
                      class="inline-flex items-center gap-1 text-xs text-amber-600 hover:text-amber-700 transition"
                      @click="warnComment(c)"
                    >
                      <AlertTriangle class="h-3.5 w-3.5" /> 警告
                    </button>
                    <button
                      v-if="!c.deleted && canDeleteComment(c.userID)"
                      class="inline-flex items-center gap-1 text-xs text-red-500 hover:text-red-600 transition"
                      @click="removeComment(c.commentID)"
                    >
                      <Trash2 class="h-3.5 w-3.5" /> 删除
                    </button>
                  </div>
                </div>
              </div>

              <div v-if="childComments(c.commentID).length" class="ml-11 mt-2 space-y-2 border-l-2 border-gray-100 pl-3">
                <div v-for="child in childComments(c.commentID)" :key="child.commentID" class="rounded-md bg-gray-50 p-3">
                  <div class="flex items-center justify-between gap-2">
                    <div class="flex items-center gap-2 text-xs">
                      <span class="font-medium text-gray-800">{{ child.userName }}</span>
                      <span class="text-brand-muted">{{ formatTime(child.commentTime) }}</span>
                    </div>
                    <div class="flex items-center gap-2">
                      <button
                        v-if="!child.deleted && !isAdmin"
                        class="inline-flex items-center gap-1 text-xs text-brand-muted hover:text-brand-primary transition"
                        @click="startReply(child)"
                      >
                        <MessageSquare class="h-3.5 w-3.5" /> 回复
                      </button>
                      <button
                        v-if="!child.deleted && isAdmin"
                        class="inline-flex items-center gap-1 text-xs text-amber-600 hover:text-amber-700 transition"
                        @click="warnComment(child)"
                      >
                        <AlertTriangle class="h-3.5 w-3.5" /> 警告
                      </button>
                      <button
                        v-if="!child.deleted && canDeleteComment(child.userID)"
                        class="inline-flex items-center gap-1 text-xs text-red-500 hover:text-red-600 transition"
                        @click="removeComment(child.commentID)"
                      >
                        <Trash2 class="h-3.5 w-3.5" /> 删除
                      </button>
                    </div>
                  </div>
                  <p class="mt-1 text-sm whitespace-pre-wrap" :class="child.deleted ? 'text-gray-400 italic' : 'text-gray-700'">
                    <span v-if="child.replyToUserName && !child.deleted" class="text-brand-primary mr-1">@{{ child.replyToUserName }}</span>
                    {{ child.commentContent }}
                  </p>
                </div>
              </div>
            </div>
          </div>
          <div v-if="!isAdmin" class="mt-4 flex flex-col gap-2">
            <div v-if="replyingTo" class="rounded-md border border-brand-primary/20 bg-brand-primary/5 px-3 py-2 text-xs text-brand-primary">
              正在回复 @{{ replyingTo.userName }}
              <button class="ml-2 underline" @click="cancelReply">取消</button>
            </div>
            <el-input
              v-model="commentText"
              type="textarea"
              :rows="3"
              :placeholder="replyingTo ? `回复 @${replyingTo.userName}…` : '写下你的评论…'"
            />
            <button class="btn-primary self-end" :disabled="posting" @click="postComment">
              <MessageSquare class="h-4 w-4" /> 发表评论
            </button>
          </div>
        </div>
      </div>

      <!-- Sidebar -->
      <div class="space-y-4">
        <div class="card p-4">
          <h3 class="font-semibold text-gray-800 text-sm mb-3">资料信息</h3>
          <dl class="space-y-2 text-sm">
            <div class="flex justify-between">
              <dt class="text-brand-muted">科目</dt>
              <dd class="text-gray-800 font-medium">{{ material.materialSubject }}</dd>
            </div>
            <div class="flex justify-between">
              <dt class="text-brand-muted">上传者</dt>
              <dd class="text-gray-800 font-medium">{{ material.uploaderName }}</dd>
            </div>
            <div class="flex justify-between">
              <dt class="text-brand-muted">类型</dt>
              <dd class="text-gray-800 font-medium">{{ material.materialType === 'pdf' ? 'PDF' : '文本' }}</dd>
            </div>
          </dl>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AlertTriangle, FileText, MessageSquare, Trash2 } from '@lucide/vue'
import http from '@/api/http'
import { useUserStore } from '@/stores/user'
import type { ApiResult } from '@/types'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const loading = ref(true)
const material = ref<any>(null)
const comments = ref<any[]>([])
const commentText = ref('')
const posting = ref(false)
const replyingTo = ref<any | null>(null)
const topLevelComments = ref<any[]>([])
const isAdmin = computed(() => store.user?.userType === 'admin')

function openPdf() {
  router.push({ name: 'materialPdfPreview', params: { id: route.params.id } })
}

async function load() {
  try {
    const res = await http.get<any, ApiResult>(`/materials/${route.params.id}`)
    if (res.success && res.data) {
      material.value = res.data.material
      comments.value = res.data.comments || []
      topLevelComments.value = comments.value.filter((c) => !toNum(c.parentCommentID))
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载资料失败')
  } finally {
    loading.value = false
  }
}

async function postComment() {
  const content = commentText.value.trim()
  if (!content) {
    ElMessage.warning('请输入讨论内容')
    return
  }
  posting.value = true
  const body = new URLSearchParams({ commentContent: content })
  if (replyingTo.value) {
    body.set('parentCommentID', String(rootCommentId(replyingTo.value)))
    body.set('replyToUserID', replyingTo.value.userID)
  }
  try {
    const res = await http.post<any, ApiResult>(`/materials/${route.params.id}/comments`, body)
    if (res.success) {
      ElMessage.success('发布成功')
      commentText.value = ''
      replyingTo.value = null
      await load()
    } else {
      ElMessage.error(res.msg || '发布失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '发布失败')
  } finally {
    posting.value = false
  }
}

function formatTime(value: string | null | undefined) {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  return d.toLocaleString('zh-CN', { hour12: false })
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
    const endpoint = isAdmin.value
      ? `/materials/${route.params.id}/comments/${commentId}/admin-delete`
      : `/materials/${route.params.id}/comments/${commentId}`
    const res = isAdmin.value
      ? await http.post<any, ApiResult>(endpoint)
      : await http.delete<any, ApiResult>(endpoint)
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

function canDeleteComment(commentUserId: string) {
  return isAdmin.value || commentUserId === store.user?.userID
}

async function warnComment(comment: any) {
  const targetUser = comment?.userID
  if (!targetUser) {
    ElMessage.error('无法识别评论作者')
    return
  }
  try {
    await ElMessageBox.confirm('确认对该用户发出一次警告？', '管理员警告', { type: 'warning' })
    const body = new URLSearchParams({
      userID: String(targetUser),
      reason: '资料评论区不当言论',
    })
    const res = await http.post<any, ApiResult>('/admin/users/warn', body)
    if (!res.success) {
      ElMessage.error(res.msg || '警告失败')
      return
    }
    const info = (res.data || {}) as any
    const warningCount = Number(info.warningCount ?? 0)
    if (info.isBanned) {
      ElMessage.success('已警告；该用户已触发封禁')
    } else {
      ElMessage.success(`已警告；当前警告次数：${warningCount}`)
    }
  } catch {
    // cancel
  }
}
onMounted(load)
</script>
