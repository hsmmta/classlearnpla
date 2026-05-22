<template>
  <div class="page-shell">
    <PageHeader title="提问" description="描述你的问题，等待同学回答" />
    <div class="card p-6 max-w-2xl">
      <el-form @submit.prevent="submit" class="space-y-2">
        <el-form-item label="标题"><el-input v-model="form.questionTitle" placeholder="简要描述问题" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.questionContent" type="textarea" :rows="8" placeholder="详细描述你的问题…" /></el-form-item>
        <el-form-item label="图片">
          <input type="file" accept="image/*" multiple @change="onImageChange" class="text-sm text-gray-600" />
          <p class="mt-1 text-xs text-brand-muted">可选，最多 3 张，每张不超过 5MB</p>
        </el-form-item>
        <div v-if="imagePreviews.length" class="grid grid-cols-3 gap-2">
          <div v-for="src in imagePreviews" :key="src" class="rounded-md overflow-hidden ring-1 ring-gray-200">
            <img :src="src" class="h-24 w-full object-cover" alt="question image preview" />
          </div>
        </div>
        <div class="flex gap-3 pt-2">
          <button type="submit" class="btn-primary"><Send class="h-4 w-4" /> 提交</button>
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
import { Send } from '@lucide/vue'
import axios from 'axios'
import type { ApiResult } from '@/types'
import PageHeader from '@/components/PageHeader.vue'

const router = useRouter()
const form = reactive({ questionTitle: '', questionContent: '' })
const imageFiles = ref<File[]>([])
const imagePreviews = ref<string[]>([])

function onImageChange(e: Event) {
  const files = Array.from((e.target as HTMLInputElement).files || [])
  if (files.length > 3) {
    ElMessage.error('最多上传 3 张图片')
    ;(e.target as HTMLInputElement).value = ''
    imageFiles.value = []
    imagePreviews.value = []
    return
  }
  for (const file of files) {
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.error('单张图片不能超过 5MB')
      ;(e.target as HTMLInputElement).value = ''
      imageFiles.value = []
      imagePreviews.value = []
      return
    }
  }
  imageFiles.value = files
  imagePreviews.value = files.map((f) => URL.createObjectURL(f))
}

async function submit() {
  const fd = new FormData()
  fd.append('questionTitle', form.questionTitle)
  fd.append('questionContent', form.questionContent)
  imageFiles.value.forEach((file) => fd.append('questionImages', file))
  const { data } = await axios.post<ApiResult>('/api/questions', fd, { withCredentials: true })
  if (data.success) { ElMessage.success('已提交审核'); router.push('/discussion') }
  else ElMessage.error(data.msg)
}
</script>
