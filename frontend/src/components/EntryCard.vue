<script setup lang="ts">
import { type Component } from 'vue'

defineProps<{
  title: string
  description: string
  to: string
  icon: Component
  badge?: string | number
  color?: 'primary' | 'secondary' | 'green' | 'amber'
}>()

const colorMap = {
  primary: 'bg-brand-primary/10 text-brand-primary',
  secondary: 'bg-brand-secondary/10 text-brand-secondary',
  green: 'bg-green-100 text-green-600',
  amber: 'bg-amber-100 text-amber-600',
}
</script>

<template>
  <router-link
    :to="to"
    class="card group flex flex-col gap-3 sm:gap-4 p-4 sm:p-5 cursor-pointer min-h-[130px]"
  >
    <div class="flex items-start justify-between">
      <span
        class="inline-flex items-center justify-center rounded-xl p-3 transition group-hover:scale-105"
        :class="colorMap[color ?? 'primary']"
      >
        <component :is="icon" class="h-5 w-5 sm:h-6 sm:w-6" />
      </span>
      <span v-if="badge !== undefined" class="label-tag">{{ badge }}</span>
    </div>
    <div>
      <h3 class="font-semibold text-gray-900 text-sm sm:text-base group-hover:text-brand-primary transition">{{ title }}</h3>
      <p class="mt-1 text-sm text-brand-muted line-clamp-2">{{ description }}</p>
    </div>
  </router-link>
</template>
