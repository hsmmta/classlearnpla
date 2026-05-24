import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: () => import('@/views/auth/LoginView.vue'), meta: { guest: true } },
    { path: '/register', name: 'register', component: () => import('@/views/auth/RegisterView.vue'), meta: { guest: true } },
    { path: '/admin/login', name: 'adminLogin', component: () => import('@/views/auth/AdminLoginView.vue'), meta: { guest: true } },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      children: [
        { path: '', name: 'home', component: () => import('@/views/home/HomeView.vue') },
        { path: 'materials', name: 'materials', component: () => import('@/views/material/MaterialListView.vue') },
        { path: 'materials/new', name: 'materialNew', component: () => import('@/views/material/MaterialUploadView.vue') },
        { path: 'materials/mine', name: 'materialMine', component: () => import('@/views/material/MaterialMineView.vue') },
        {
          path: 'materials/:id',
          name: 'materialDetail',
          component: () => import('@/views/material/MaterialDetailView.vue'),
          meta: { allowAdmin: true },
        },
        {
          path: 'materials/:id/pdf',
          name: 'materialPdfPreview',
          component: () => import('@/views/material/MaterialPdfView.vue'),
          meta: { allowAdmin: true },
        },
        { path: 'materials/:id/edit', name: 'materialEdit', component: () => import('@/views/material/MaterialEditView.vue') },
        { path: 'discussion', name: 'discussion', component: () => import('@/views/discussion/DiscussionListView.vue') },
        { path: 'discussion/ask', name: 'discussionAsk', component: () => import('@/views/discussion/AskQuestionView.vue') },
        { path: 'discussion/mine', name: 'discussionMine', component: () => import('@/views/discussion/MyDiscussionView.vue') },
        {
          path: 'discussion/:id',
          name: 'discussionDetail',
          component: () => import('@/views/discussion/QuestionDetailView.vue'),
          meta: { allowAdmin: true },
        },
        { path: 'prizes', name: 'prizes', component: () => import('@/views/prize/PrizeShopView.vue') },
        { path: 'prizes/history', name: 'prizeHistory', component: () => import('@/views/prize/ExchangeHistoryView.vue') },
        { path: 'points/history', name: 'pointsHistory', component: () => import('@/views/prize/ExchangeHistoryView.vue') },
        { path: 'profile', name: 'profile', component: () => import('@/views/profile/ProfileView.vue') },
        { path: 'profile/password', name: 'profilePassword', component: () => import('@/views/profile/ChangePasswordView.vue') },
        { path: 'profile/forgot', name: 'profileForgot', component: () => import('@/views/profile/ForgotPasswordView.vue'), meta: { guest: true } },
      ],
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAdmin: true },
      children: [
        { path: '', name: 'adminHome', component: () => import('@/views/admin/AdminHomeView.vue') },
        { path: 'materials', name: 'adminMaterials', component: () => import('@/views/admin/AuditMaterialView.vue') },
        { path: 'questions', name: 'adminQuestions', component: () => import('@/views/admin/AuditQuestionView.vue') },
        { path: 'prizes', name: 'adminPrizes', component: () => import('@/views/admin/ManagePrizeView.vue') },
        { path: 'points', name: 'adminPoints', component: () => import('@/views/admin/ModifyPointsView.vue') },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const store = useUserStore()
  if (!store.loaded) await store.fetchSession()

  if (to.meta.guest) return true

  if (to.meta.requiresAdmin) {
    if (!store.user || store.user.userType !== 'admin') return '/admin/login'
    return true
  }

  if (to.path.startsWith('/admin/login')) return true

  const publicPaths = ['/login', '/register', '/profile/forgot']
  if (!store.user && !publicPaths.includes(to.path)) {
    return '/login'
  }
  if (store.user?.userType === 'admin' && !to.path.startsWith('/admin') && !to.meta.allowAdmin) {
    return '/admin'
  }
  return true
})

export default router
