<template>
  <div>
    <!-- Hero -->
    <div class="bg-gradient-to-br from-brand-primary to-brand-dark px-4 pt-10 sm:pt-12 pb-14 sm:pb-16 text-center">
      <h1 class="text-2xl sm:text-4xl font-bold text-white tracking-tight">
        欢迎，{{ user?.userName || '同学' }}
      </h1>
      <p class="mt-2 sm:mt-3 text-white/75 text-sm sm:text-lg max-w-lg mx-auto">
        学习资料、互动讨论、积分奖励，一站搞定
      </p>
    </div>
    <!-- Entry cards -->
    <div class="page-shell -mt-7 sm:-mt-8">
      <div class="grid gap-3 sm:gap-4 grid-cols-2 lg:grid-cols-3">
        <EntryCard
          title="资料藏宝阁"
          description="浏览和下载同学上传的学习资料"
          to="/materials"
          :icon="BookOpen"
          color="primary"
        />
        <EntryCard
          title="班级讨论区"
          description="提问、回答，分享你的想法"
          to="/discussion"
          :icon="MessageSquare"
          color="secondary"
        />
        <EntryCard
          title="我的讨论"
          description="查看自己发起的讨论和审核状态"
          to="/discussion/mine"
          :icon="MessageSquare"
          color="green"
        />
        <EntryCard
          title="积分商城"
          description="用积分换取心仪的商品"
          to="/prizes"
          :icon="Gift"
          color="amber"
        />
        <EntryCard
          title="个人中心"
          description="管理你的个人信息和密码"
          to="/profile"
          :icon="User"
          color="green"
        />
        <EntryCard
          title="我的资料"
          description="查看你上传的全部资料"
          to="/materials/mine"
          :icon="FolderOpen"
          color="primary"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import EntryCard from '@/components/EntryCard.vue'
import { BookOpen, MessageSquare, Gift, User, FolderOpen } from '@lucide/vue'

const user = computed(() => useUserStore().user)
</script>
