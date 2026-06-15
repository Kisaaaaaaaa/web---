import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login', name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', noAuth: true }
  },
  {
    path: '/register', name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', noAuth: true }
  },
  {
    path: '/', component: () => import('@/views/Layout.vue'),
    redirect: (to) => {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
      return userInfo?.roleCode === 'ROLE_ADMIN' ? '/admin/dashboard' : '/home'
    },
    children: [
      { path: 'home', name: 'Home', component: () => import('@/views/Home.vue'), meta: { title: '首页' } },
      { path: 'books', name: 'BookList', component: () => import('@/views/book/BookList.vue'), meta: { title: '图书列表' } },
      { path: 'books/:id', name: 'BookDetail', component: () => import('@/views/book/BookDetail.vue'), meta: { title: '图书详情' } },
      { path: 'borrows', name: 'BorrowHistory', component: () => import('@/views/borrow/BorrowHistory.vue'), meta: { title: '我的借阅' } },
      { path: 'favorites', name: 'FavoriteList', component: () => import('@/views/favorite/FavoriteList.vue'), meta: { title: '我的收藏' } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { title: '个人主页' } },
      // admin
      { path: 'admin/users', name: 'UserManage', component: () => import('@/views/admin/UserManage.vue'), meta: { title: '用户管理', requireAdmin: true } },
      { path: 'admin/books', name: 'BookManage', component: () => import('@/views/admin/BookManage.vue'), meta: { title: '图书管理', requireAdmin: true } },
      { path: 'admin/borrows', name: 'BorrowManage', component: () => import('@/views/admin/BorrowManage.vue'), meta: { title: '借阅管理', requireAdmin: true } },
      { path: 'admin/categories', name: 'CategoryManage', component: () => import('@/views/admin/CategoryManage.vue'), meta: { title: '分类管理', requireAdmin: true } },
      { path: 'admin/carousels', name: 'CarouselManage', component: () => import('@/views/admin/CarouselManage.vue'), meta: { title: '轮播图管理', requireAdmin: true } },
      { path: 'admin/announcements', name: 'AnnounceManage', component: () => import('@/views/admin/AnnounceManage.vue'), meta: { title: '公告管理', requireAdmin: true } },
      { path: 'admin/dashboard', name: 'Dashboard', component: () => import('@/views/admin/Dashboard.vue'), meta: { title: '数据仪表盘', requireAdmin: true } },
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/home' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
  document.title = to.meta.title ? `${to.meta.title} - 校园图书借阅系统` : '校园图书借阅自助管理系统'
  if (to.meta.noAuth) {
    if (token && to.path === '/login') return next('/home')
    return next()
  }
  if (!token) return next('/login')
  if (to.meta.requireAdmin && userInfo?.roleCode !== 'ROLE_ADMIN') return next('/home')
  // 学生页面：管理员不能访问（管理员直接跳转仪表盘）
  const studentPages = ['/home', '/borrows', '/favorites']
  if (userInfo?.roleCode === 'ROLE_ADMIN' && studentPages.some(p => to.path.startsWith(p))) {
    return next('/admin/dashboard')
  }
  next()
})

export default router
