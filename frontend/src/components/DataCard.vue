<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
  meta?: string
  tag?: string
  to?: string
}>()
</script>

<template>
  <component
    :is="to ? 'router-link' : 'div'"
    :to="to"
    class="card group flex flex-col gap-3 p-4 cursor-pointer"
    :class="to ? 'hover:ring-brand-primary/40' : ''"
  >
    <div class="flex items-start justify-between gap-2">
      <h3 class="font-medium text-gray-900 text-sm leading-snug break-words group-hover:text-brand-primary transition line-clamp-2">
        {{ title }}
      </h3>
      <span v-if="tag" class="label-tag shrink-0">{{ tag }}</span>
    </div>
    <p v-if="subtitle" class="text-xs text-brand-muted line-clamp-2">{{ subtitle }}</p>
    <div v-if="meta || $slots.footer" class="flex items-center justify-between pt-2 border-t border-gray-100 mt-auto">
      <span v-if="meta" class="text-xs text-gray-400">{{ meta }}</span>
      <slot name="footer" />
    </div>
  </component>
</template>
