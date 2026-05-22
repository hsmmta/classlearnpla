<template>
  <div class="page-shell">
    <PageHeader title="上传资料" description="上传后需等待管理员审核" />
    <div class="card p-6 max-w-2xl">
      <el-form label-width="80px" @submit.prevent="onSubmit" class="space-y-2">
        <el-form-item label="标题"><el-input v-model="form.materialTitle" placeholder="请输入资料标题" /></el-form-item>
        <el-form-item label="科目"><el-input v-model="form.materialSubject" placeholder="如：数学、英语…" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.materialType">
            <el-radio value="text">文本</el-radio>
            <el-radio value="pdf">PDF</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.materialType === 'text'" label="内容">
          <el-input v-model="form.materialContent" type="textarea" :rows="10" placeholder="请输入资料内容" />
        </el-form-item>
        <el-form-item v-else label="PDF 文件">
          <input type="file" accept=".pdf" @change="onFile" class="text-sm text-gray-600" />
          <p class="mt-1 text-xs text-brand-muted">单个 PDF 最大 30MB</p>
        </el-form-item>
        <div class="flex gap-3 pt-2">
          <button type="submit" class="btn-primary" :disabled="loading">
            <Upload class="h-4 w-4" />
            {{ loading ? '上传中…' : '提交审核' }}
          </button>
          <button type="button" class="btn-ghost" @click="$router.back()">取消</button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Upload } from '@lucide/vue'
import axios from 'axios'
import type { ApiResult } from '@/types'
import PageHeader from '@/components/PageHeader.vue'

const router = useRouter()
const loading = ref(false)
const pdfFile = ref<File | null>(null)
const form = reactive({ materialTitle: '', materialSubject: '', materialType: 'text', materialContent: '' })

function onFile(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0] || null
  if (file && file.size > 30 * 1024 * 1024) {
    ElMessage.error('PDF 文件不能超过 30MB')
    ;(e.target as HTMLInputElement).value = ''
    pdfFile.value = null
    return
  }
  pdfFile.value = file
}

async function onSubmit() {
  loading.value = true
  try {
    const fd = new FormData()
    Object.entries(form).forEach(([k, v]) => fd.append(k, v))
    if (form.materialType === 'pdf' && pdfFile.value) fd.append('pdfFile', pdfFile.value)
    const { data } = await axios.post<ApiResult>('/api/materials', fd, { withCredentials: true })
    if (data.success) { ElMessage.success('上传成功'); router.push('/materials/mine') }
    else ElMessage.error(data.msg)
  } finally { loading.value = false }
}
</script>
