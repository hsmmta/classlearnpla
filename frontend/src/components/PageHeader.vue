<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<template>
  <div class="flex flex-wrap items-start justify-between gap-4 mb-6">
    <div>
      <h1 class="section-title">{{ title }}</h1>
      <p v-if="description" class="mt-1 text-sm text-brand-muted">{{ description }}</p>
    </div>
    <div class="flex flex-wrap items-center gap-2 shrink-0">
      <slot />
    </div>
  </div>
</template>
