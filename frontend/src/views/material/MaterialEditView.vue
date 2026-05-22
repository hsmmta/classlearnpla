<template>
  <div class="page-shell">
    <PageHeader title="编辑资料" />
    <div class="card p-6 max-w-2xl">
      <el-form label-width="80px" @submit.prevent="save" class="space-y-2">
        <el-form-item label="标题"><el-input v-model="form.materialTitle" /></el-form-item>
        <el-form-item label="科目"><el-input v-model="form.materialSubject" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.materialContent" type="textarea" :rows="10" /></el-form-item>
        <div class="flex gap-3 pt-2">
          <button type="submit" class="btn-primary"><Save class="h-4 w-4" /> 保存</button>
          <button type="button" class="btn-ghost" @click="$router.back()">取消</button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Save } from '@lucide/vue'
import http from '@/api/http'
import type { ApiResult } from '@/types'
import PageHeader from '@/components/PageHeader.vue'

const route = useRoute()
const router = useRouter()
const form = reactive({ materialTitle: '', materialSubject: '', materialContent: '' })

onMounted(async () => {
  const res = await http.get<any, ApiResult>(`/materials/${route.params.id}`)
  if (res.success && res.data?.material) Object.assign(form, {
    materialTitle: res.data.material.materialTitle,
    materialSubject: res.data.material.materialSubject,
    materialContent: res.data.material.materialContent,
  })
})

async function save() {
  const res = await http.put<any, ApiResult>(`/materials/${route.params.id}`, null, {
    params: {
      materialTitle: form.materialTitle ?? '',
      materialSubject: form.materialSubject ?? '',
      materialContent: form.materialContent ?? '',
    },
  })
  if (res.success) { ElMessage.success('已保存'); router.push('/materials/mine') }
  else ElMessage.error(res.msg)
}
</script>
