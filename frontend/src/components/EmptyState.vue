<script setup lang="ts">
import { Inbox } from '@lucide/vue'

defineProps<{
  message?: string
}>()
</script>

<template>
  <div class="flex flex-col items-center justify-center py-16 text-brand-muted gap-3">
    <Inbox class="h-12 w-12 opacity-40" />
    <p class="text-sm">{{ message ?? '暂无数据' }}</p>
    <slot />
  </div>
</template>
