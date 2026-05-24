<template>
  <div class="page-shell">
    <PageHeader title="PDF 图片预览" description="兼容模式：服务端转图片后展示" />
    <div class="mb-3 flex flex-wrap items-center gap-2">
      <button class="btn-ghost !px-3 !py-2 text-sm" @click="goBack">返回资料详情</button>
      <button class="btn-ghost !px-3 !py-2 text-sm" @click="downloadPdf">下载 PDF</button>
      <button v-if="isMobile" class="btn-ghost !px-3 !py-2 text-sm" @click="openInSystemBrowser">系统浏览器下载</button>
      <button v-if="isMobile" class="btn-ghost !px-3 !py-2 text-sm" @click="copyDownloadLink">复制下载链接</button>
    </div>
    <div class="card p-3 sm:p-4">
      <div v-if="loading" class="py-12 text-center text-sm text-brand-muted">正在生成预览图，请稍候...</div>
      <div v-else-if="errorMsg" class="py-12 text-center text-sm text-red-500">{{ errorMsg }}</div>
      <div v-else class="space-y-3">
        <div class="text-xs text-brand-muted">共 {{ pageCount }} 页（图片预览）</div>
        <img
          v-for="(url, idx) in images"
          :key="url"
          :src="url"
          :alt="`pdf-page-${idx + 1}`"
          loading="lazy"
          class="w-full rounded-md border border-gray-200 bg-white"
        />
      </div>
      <div class="mt-3 flex justify-end">
        <button class="btn-primary !px-3 !py-2 text-sm" @click="downloadPdf">下载到设备打开</button>
      </div>
      <div v-if="isMobile" class="mt-3 rounded-md bg-gray-50 px-3 py-2 text-xs text-brand-muted break-all">
        下载链接：{{ absoluteAttachmentUrl }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import http from '@/api/http'
import type { ApiResult } from '@/types'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const pdfUrl = computed(() => `/api/materials/${route.params.id}/download`)
const attachmentUrl = computed(() => `/api/materials/${route.params.id}/download-attachment`)
const absoluteAttachmentUrl = computed(() => new URL(attachmentUrl.value, window.location.origin).toString())
const isMobile = /Android|iPhone|iPad|iPod|HarmonyOS/i.test(navigator.userAgent)
const loading = ref(true)
const errorMsg = ref('')
const pageCount = ref(0)
const images = ref<string[]>([])

function goBack() {
  router.push({ name: 'materialDetail', params: { id: route.params.id } })
}

function downloadPdf() {
  // Use attachment endpoint for mobile WebView compatibility.
  window.location.href = attachmentUrl.value
}

function openInSystemBrowser() {
  const url = absoluteAttachmentUrl.value
  const w = window.open(url, '_blank', 'noopener,noreferrer')
  if (!w) {
    // Some WebViews block popups; fallback to direct navigation.
    window.location.href = url
  }
}

async function copyDownloadLink() {
  const text = absoluteAttachmentUrl.value
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(text)
      ElMessage.success('下载链接已复制')
      return
    }
  } catch {
    // fallback below
  }
  const input = document.createElement('input')
  input.value = text
  document.body.appendChild(input)
  input.select()
  document.execCommand('copy')
  document.body.removeChild(input)
  ElMessage.success('下载链接已复制')
}

async function loadPreviewImages() {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await http.get<any, ApiResult>(`/materials/${route.params.id}/preview-images`)
    if (!res.success) {
      errorMsg.value = res.msg || '加载预览失败'
      return
    }
    const data = (res.data || {}) as any
    pageCount.value = Number(data.pageCount || 0)
    images.value = Array.isArray(data.images) ? data.images : []
    if (!images.value.length) {
      errorMsg.value = '未生成可用预览图，请直接下载查看'
    }
  } catch (e: any) {
    errorMsg.value = e?.message || '加载预览失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadPreviewImages)
</script>
