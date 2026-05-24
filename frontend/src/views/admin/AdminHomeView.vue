<template>
  <div class="page-shell">
    <PageHeader title="管理后台" description="欢迎使用班级学习社区管理系统" />
    <div class="grid gap-3 sm:gap-4 grid-cols-2 lg:grid-cols-4">
      <EntryCard title="资料审核" description="审核用户上传的学习资料" to="/admin/materials" :icon="FileText" color="primary" />
      <EntryCard title="问题审核" description="审核用户发布的讨论问题" to="/admin/questions" :icon="MessageSquare" color="secondary" />
      <EntryCard title="奖品管理" description="上架或下架积分商城商品" to="/admin/prizes" :icon="Gift" color="amber" />
      <EntryCard title="积分管理" description="查询并修改用户积分" to="/admin/points" :icon="Coins" color="green" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { FileText, MessageSquare, Gift, Coins } from '@lucide/vue'
import PageHeader from '@/components/PageHeader.vue'
import EntryCard from '@/components/EntryCard.vue'
</script>
