<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Menu, X, LogOut, BookOpen } from '@lucide/vue'

const store = useUserStore()
const router = useRouter()
const route = useRoute()
const user = computed(() => store.user)
const open = ref(false)

const navLinks = [
  { to: '/', label: '首页' },
  { to: '/materials', label: '资料区' },
  { to: '/discussion', label: '讨论区' },
  { to: '/discussion/mine', label: '我的讨论' },
  { to: '/prizes', label: '积分商城' },
  { to: '/profile', label: '个人中心' },
]

function isActive(to: string) {
  if (to === '/') return route.path === '/'
  if (to === '/discussion') return route.path === '/discussion' || (route.path.startsWith('/discussion/') && !route.path.startsWith('/discussion/mine'))
  return route.path.startsWith(to)
}

async function logout() {
  open.value = false
  await store.logout()
  router.push('/login')
}
</script>

<template>
  <header class="sticky top-0 z-30 bg-white/90 backdrop-blur ring-1 ring-gray-100">
    <div class="mx-auto flex max-w-6xl items-center justify-between px-4 sm:px-6 lg:px-8 h-16">
      <!-- Logo -->
      <router-link to="/" class="flex items-center gap-2 font-bold text-brand-primary text-lg select-none">
        <BookOpen class="h-5 w-5 shrink-0" />
        <span>班级学习社区</span>
      </router-link>

      <!-- Desktop nav -->
      <nav class="hidden md:flex items-center gap-1">
        <router-link
          v-for="link in navLinks"
          :key="link.to"
          :to="link.to"
          class="rounded-md px-3 py-2 text-sm font-medium transition"
          :class="isActive(link.to)
            ? 'bg-brand-primary/10 text-brand-primary'
            : 'text-gray-600 hover:text-brand-primary hover:bg-gray-50'"
        >
          {{ link.label }}
        </router-link>
      </nav>

      <!-- Desktop user area -->
      <div class="hidden md:flex items-center gap-3">
        <span v-if="user" class="text-sm text-gray-600 truncate max-w-[120px]">{{ user.userName }}</span>
        <button v-if="user" @click="logout" class="btn-ghost !px-3 !py-1.5 text-xs">
          <LogOut class="h-3.5 w-3.5" />
          退出
        </button>
        <router-link v-else to="/login" class="btn-primary !px-3 !py-1.5 text-xs">登录</router-link>
      </div>

      <!-- Mobile hamburger -->
      <button @click="open = !open" class="md:hidden p-2 rounded-md text-gray-600 hover:bg-gray-100 transition">
        <X v-if="open" class="h-5 w-5" />
        <Menu v-else class="h-5 w-5" />
      </button>
    </div>
  </header>

  <!-- Mobile full-screen drawer -->
  <Transition
    enter-active-class="transition duration-200"
    enter-from-class="opacity-0"
    enter-to-class="opacity-100"
    leave-active-class="transition duration-150"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0"
  >
    <div
      v-if="open"
      class="fixed inset-0 z-20 flex flex-col bg-brand-dark md:hidden"
      style="padding-top: env(safe-area-inset-top)"
    >
      <!-- Drawer header -->
      <div class="flex items-center justify-between px-5 h-16">
        <span class="text-white font-bold text-lg">班级学习社区</span>
        <button @click="open = false" class="p-2 text-white hover:text-brand-secondary transition">
          <X class="h-5 w-5" />
        </button>
      </div>

      <!-- Links -->
      <nav class="flex flex-col mt-4 px-4 gap-1">
        <router-link
          v-for="link in navLinks"
          :key="link.to"
          :to="link.to"
          @click="open = false"
          class="rounded-md px-4 py-3 text-base font-medium text-white/90 border-b border-white/10 hover:text-white hover:bg-white/10 transition"
          :class="isActive(link.to) ? 'text-brand-secondary border-brand-secondary/40' : ''"
        >
          {{ link.label }}
        </router-link>
      </nav>

      <!-- Bottom user area -->
      <div class="mt-auto px-5 pb-8" style="padding-bottom: max(2rem, env(safe-area-inset-bottom))">
        <div v-if="user" class="flex flex-col gap-3">
          <span class="text-white/70 text-sm">{{ user.userName }}</span>
          <button @click="logout" class="btn-secondary w-full justify-center">
            <LogOut class="h-4 w-4" />
            退出登录
          </button>
        </div>
        <router-link v-else to="/login" @click="open = false" class="btn-primary w-full justify-center">
          登录
        </router-link>
      </div>
    </div>
  </Transition>
</template>
