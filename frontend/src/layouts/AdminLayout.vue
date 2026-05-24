<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Menu, X, LayoutDashboard, FileText, MessageSquare, Gift, Coins, LogOut } from '@lucide/vue'

const store = useUserStore()
const router = useRouter()
const route = useRoute()
const user = computed(() => store.user)
const open = ref(false)

const navLinks = [
  { to: '/admin', label: '概览', icon: LayoutDashboard, exact: true },
  { to: '/admin/materials', label: '资料审核', icon: FileText },
  { to: '/admin/questions', label: '问题审核', icon: MessageSquare },
  { to: '/admin/prizes', label: '奖品管理', icon: Gift },
  { to: '/admin/points', label: '积分管理', icon: Coins },
]

function isActive(link: { to: string; exact?: boolean }) {
  if (link.exact) return route.path === link.to
  return route.path.startsWith(link.to)
}

async function logout() {
  open.value = false
  await store.logout()
  router.push('/admin/login')
}
</script>

<template>
  <div class="min-h-screen flex flex-col bg-gray-50">
    <!-- Top bar -->
    <header class="sticky top-0 z-30 bg-brand-dark text-white ring-1 ring-white/10">
      <div class="flex items-center justify-between px-4 h-14 max-w-full">
        <div class="flex items-center gap-3">
          <button @click="open = !open" class="md:hidden p-2 rounded text-white/70 hover:text-white hover:bg-white/10 transition">
            <X v-if="open" class="h-5 w-5" />
            <Menu v-else class="h-5 w-5" />
          </button>
          <span class="font-bold tracking-wide text-sm">管理后台</span>
        </div>
        <div class="flex items-center gap-3">
          <span class="text-sm text-white/70 hidden sm:block truncate max-w-[120px]">{{ user?.userName }}</span>
          <button @click="logout" class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded text-xs font-medium text-white/80 hover:text-white hover:bg-white/10 transition">
            <LogOut class="h-3.5 w-3.5" />
            退出
          </button>
        </div>
      </div>
    </header>

    <div class="flex flex-1">
      <!-- Desktop sidebar -->
      <aside class="hidden md:flex flex-col w-52 shrink-0 bg-white border-r border-gray-200 min-h-0">
        <nav class="flex flex-col gap-0.5 p-3 mt-2">
          <router-link
            v-for="link in navLinks"
            :key="link.to"
            :to="link.to"
            class="flex items-center gap-2.5 rounded-md px-3 py-2.5 text-sm font-medium transition"
            :class="isActive(link)
              ? 'bg-brand-primary text-white'
              : 'text-gray-600 hover:bg-gray-50 hover:text-brand-primary'"
          >
            <component :is="link.icon" class="h-4 w-4 shrink-0" />
            {{ link.label }}
          </router-link>
        </nav>
      </aside>

      <!-- Mobile drawer overlay -->
      <Transition
        enter-active-class="transition duration-200"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="transition duration-150"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <div v-if="open" class="fixed inset-0 z-20 flex md:hidden">
          <div class="w-60 bg-brand-dark flex flex-col pt-4">
            <nav class="flex flex-col gap-0.5 px-3">
              <router-link
                v-for="link in navLinks"
                :key="link.to"
                :to="link.to"
                @click="open = false"
                class="flex items-center gap-2.5 rounded-md px-3 py-3 text-sm font-medium text-white/80 hover:text-white hover:bg-white/10 transition"
                :class="isActive(link) ? '!text-brand-secondary' : ''"
              >
                <component :is="link.icon" class="h-4 w-4 shrink-0" />
                {{ link.label }}
              </router-link>
            </nav>
          </div>
          <div class="flex-1 bg-black/40" @click="open = false" />
        </div>
      </Transition>

      <!-- Page content -->
      <main class="flex-1 min-w-0 overflow-auto app-safe-bottom">
        <router-view />
      </main>
    </div>
  </div>
</template>
