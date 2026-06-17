<template>
  <div class="layout-top">
    <header class="top-header">
      <div class="header-inner">
        <div class="header-logo" @click="userStore.isAdmin ? $router.push('/admin/dashboard') : $router.push('/home')">
          <el-icon :size="22" class="logo-icon"><Reading /></el-icon>
          <span class="logo-text">校园图书借阅</span>
        </div>

        <nav class="header-nav">
          <template v-if="userStore.isAdmin">
            <router-link to="/admin/dashboard" class="nav-item" :class="{ active: $route.path === '/admin/dashboard' }"><el-icon class="ni-icon"><DataBoard /></el-icon>仪表盘</router-link>
            <router-link to="/admin/users" class="nav-item" :class="{ active: $route.path === '/admin/users' }"><el-icon class="ni-icon"><User /></el-icon>用户</router-link>
            <router-link to="/admin/books" class="nav-item" :class="{ active: $route.path === '/admin/books' }"><el-icon class="ni-icon"><Reading /></el-icon>图书</router-link>
            <router-link to="/admin/borrows" class="nav-item" :class="{ active: $route.path === '/admin/borrows' }"><el-icon class="ni-icon"><Notebook /></el-icon>借阅</router-link>
            <router-link to="/admin/fines" class="nav-item" :class="{ active: $route.path === '/admin/fines' }"><el-icon class="ni-icon"><Coin /></el-icon>罚金</router-link>
            <router-link to="/admin/categories" class="nav-item" :class="{ active: $route.path === '/admin/categories' }"><el-icon class="ni-icon"><Folder /></el-icon>分类</router-link>
            <router-link to="/admin/carousels" class="nav-item" :class="{ active: $route.path === '/admin/carousels' }"><el-icon class="ni-icon"><Picture /></el-icon>轮播</router-link>
            <router-link to="/admin/announcements" class="nav-item" :class="{ active: $route.path === '/admin/announcements' }"><el-icon class="ni-icon"><Bell /></el-icon>公告</router-link>
          </template>
          <template v-else>
            <router-link to="/home" class="nav-item" :class="{ active: $route.path === '/home' }"><el-icon class="ni-icon"><HomeFilled /></el-icon>首页</router-link>
            <router-link to="/books" class="nav-item" :class="{ active: $route.path.startsWith('/books') }"><el-icon class="ni-icon"><Reading /></el-icon>图书列表</router-link>
            <router-link to="/borrows" class="nav-item" :class="{ active: $route.path.startsWith('/borrows') }"><el-icon class="ni-icon"><Notebook /></el-icon>我的借阅</router-link>
            <router-link to="/fines" class="nav-item" :class="{ active: $route.path === '/fines' }"><el-icon class="ni-icon"><Coin /></el-icon>我的罚金</router-link>
            <router-link to="/favorites" class="nav-item" :class="{ active: $route.path.startsWith('/favorites') }"><el-icon class="ni-icon"><Star /></el-icon>我的收藏</router-link>
          </template>
        </nav>

        <div class="header-user">
          <span class="role-dot" :class="userStore.isAdmin ? 'dot-danger' : 'dot-primary'"></span>
          <span class="role-label">{{ userStore.isAdmin ? '管理员' : '学生' }}</span>
          <el-dropdown trigger="click">
            <span class="user-trigger">
              <el-avatar :size="34" :src="userStore.avatarUrl" class="nav-avatar">
                {{ userStore.avatarUrl ? '' : (userStore.realName?.charAt(0) || 'U') }}
              </el-avatar>
              <span class="user-name">{{ userStore.realName }}</span>
              <el-icon :size="12"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/profile')"><el-icon><User /></el-icon> 个人主页</el-dropdown-item>
                <el-dropdown-item v-if="!userStore.isAdmin" @click="$router.push('/borrows')"><el-icon><Notebook /></el-icon> 我的借阅</el-dropdown-item>
                <el-dropdown-item v-if="!userStore.isAdmin" @click="$router.push('/fines')"><el-icon><Coin /></el-icon> 我的罚金</el-dropdown-item>
                <el-dropdown-item v-if="!userStore.isAdmin" @click="$router.push('/favorites')"><el-icon><Star /></el-icon> 我的收藏</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout"><el-icon><SwitchButton /></el-icon> 退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <el-dialog v-model="overdueVisible" title="逾期提醒" width="400px" :close-on-click-modal="false" top="15vh">
      <div style="text-align:center;padding:12px 0">
        <el-icon :size="56" color="#ef4444"><WarningFilled /></el-icon>
        <p style="font-size:18px;font-weight:700;margin:16px 0 4px">您有图书已逾期！</p>
        <p style="color:var(--text-secondary);font-size:14px">共 <strong style="color:var(--danger);font-size:20px">{{ overdueCount }}</strong> 本未还，请尽快处理</p>
      </div>
      <template #footer>
        <el-button @click="$router.push('/borrows'); overdueVisible = false" type="primary" size="large" style="width:100%">去归还</el-button>
      </template>
    </el-dialog>

    <main class="main-content"><router-view /></main>

    <footer class="app-footer">
      <div class="footer-inner">
        <span>© 2026 校园图书借阅自助管理系统</span>
        <span class="dot">·</span>
        <span>图书馆信息技术部</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { ArrowDown, User, Notebook, Star, SwitchButton, Coin, Reading, HomeFilled, DataBoard, Folder, Picture, Bell, WarningFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getBorrowStats } from '@/api/borrow'

const router = useRouter()
const userStore = useUserStore()
const overdueVisible = ref(false)
const overdueCount = ref(0)

onMounted(async () => {
  if (!userStore.avatarUrl) { const c = localStorage.getItem('avatar'); if (c) userStore.setAvatar(c) }
  if (!userStore.isAdmin) {
    try { const r = await getBorrowStats(); overdueCount.value = r.data?.overdueCount || 0; if (overdueCount.value > 0) setTimeout(() => { overdueVisible.value = true }, 1200) } catch {}
  }
})

async function handleLogout() {
  try { await ElMessageBox.confirm('确定退出？', '提示', { type: 'warning' }); userStore.logout(); router.push('/login') } catch {}
}
</script>

<style scoped>
.layout-top { display: flex; flex-direction: column; min-height: 100vh; }

/* Header */
.top-header {
  position: sticky; top: 0; z-index: 200;
  background: rgba(255,255,255,.95); backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border);
  box-shadow: 0 1px 3px rgba(0,0,0,.03);
}
.header-inner {
  display: flex; align-items: center;
  max-width: 1240px; margin: 0 auto; height: 56px; padding: 0 24px;
}
.header-logo {
  display: flex; align-items: center; gap: 8px;
  cursor: pointer; flex-shrink: 0; margin-right: 28px; user-select: none;
}
.logo-icon { color: var(--primary); }
.logo-text { font-size: 17px; font-weight: 700; color: var(--text-primary); letter-spacing: .3px; white-space: nowrap; }

.header-nav { display: flex; align-items: center; gap: 2px; flex: 1; overflow-x: auto; scrollbar-width: none; }
.header-nav::-webkit-scrollbar { display: none; }
.nav-item {
  display: inline-flex; align-items: center; gap: 5px;
  height: 56px; padding: 0 14px; font-size: 14px; font-weight: 500;
  color: var(--text-secondary); text-decoration: none;
  cursor: pointer; white-space: nowrap;
  border-bottom: 3px solid transparent;
  transition: all .2s;
}
.nav-item:hover { color: var(--primary); background: var(--primary-light); }
.nav-item.active { color: var(--primary); border-bottom-color: var(--primary); background: transparent; }
.ni-icon { font-size: 16px; }

.header-user { display: flex; align-items: center; gap: 12px; flex-shrink: 0; margin-left: 16px; }
.role-dot { width: 8px; height: 8px; border-radius: 50%; }
.dot-primary { background: var(--primary); }
.dot-danger { background: var(--danger); }
.role-label { font-size: 13px; color: var(--text-secondary); }
.user-trigger { display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 4px 8px; border-radius: 24px; transition: background .2s; }
.user-trigger:hover { background: #f8fafc; }
.nav-avatar { background: linear-gradient(135deg, var(--primary), var(--purple)); color: #fff; font-weight: 700; flex-shrink: 0; }
.user-name { font-size: 14px; font-weight: 500; color: var(--text-primary); max-width: 70px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* Main */
.main-content { flex: 1; width: 100%; max-width: 1240px; margin: 0 auto; padding: 24px 24px 48px; }

/* Footer */
.app-footer { background: var(--surface); border-top: 1px solid var(--border); padding: 18px 24px; }
.footer-inner { max-width: 1240px; margin: 0 auto; text-align: center; font-size: 13px; color: var(--text-muted); display: flex; justify-content: center; gap: 8px; }
.dot { color: #cbd5e1; }

@media (max-width: 768px) {
  .header-inner { padding: 0 12px; }
  .logo-text { display: none; }
  .nav-item { padding: 0 8px; font-size: 13px; }
  .role-label, .user-name { display: none; }
  .main-content { padding: 16px 12px 32px; }
}
</style>
