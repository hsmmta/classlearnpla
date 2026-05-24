<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<template>
  <div class="flex flex-wrap items-start justify-between gap-3 sm:gap-4 mb-5 sm:mb-6">
    <div class="min-w-0">
      <h1 class="section-title">{{ title }}</h1>
      <p v-if="description" class="mt-1 text-sm text-brand-muted">{{ description }}</p>
    </div>
    <div class="flex w-full sm:w-auto flex-wrap items-center gap-2 shrink-0">
      <slot />
    </div>
  </div>
</template>
