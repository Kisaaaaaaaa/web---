<template>
  <div class="login-page">
    <div class="login-card">
      <div class="lc-left">
        <div class="brand">
          <el-icon :size="52" class="brand-icon"><Reading /></el-icon>
          <h1>校园图书借阅</h1>
          <p>自助借阅 · 轻松管理 · 畅享阅读</p>
          <div class="features">
            <div class="feat"><el-icon><Search /></el-icon>在线检索图书</div>
            <div class="feat"><el-icon><Notebook /></el-icon>自助借阅归还</div>
            <div class="feat"><el-icon><AlarmClock /></el-icon>实时逾期提醒</div>
          </div>
        </div>
      </div>
      <div class="lc-right">
        <h2>欢迎回来</h2>
        <p class="sub">请登录您的账号</p>
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
          </el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" native-type="submit">
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form>
        <p class="toggle">还没有账号？<router-link to="/register">立即注册</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Reading, Search, Notebook, AlarmClock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter(); const route = useRoute(); const userStore = useUserStore()
const formRef = ref(null); const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = { username: [{ required: true, message: '请输入用户名' }], password: [{ required: true, min: 6, message: '请输入密码' }] }

async function handleLogin() {
  const v = await formRef.value.validate().catch(() => false); if (!v) return
  loading.value = true
  try { await userStore.doLogin(form.username, form.password); ElMessage.success('登录成功'); router.push(route.query.redirect || '/') } catch {} finally { loading.value = false }
}
</script>

<style scoped>
/*
 * 背景图片路径：请将图片放入 frontend/public/ 目录下
 * 然后修改下方 url 为实际文件名，例如 url('/bg-login.jpg')
 */
.login-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: url('/bg-login.jpg') center/cover no-repeat;
  background-color: #1e293b;   /* 图片加载前的兜底色 */
  position: relative; padding: 24px;
}
.login-card { display: flex; width: 100%; max-width: 820px; background: #fff; border-radius: var(--radius-xl); box-shadow: 0 25px 80px rgba(0,0,0,.25); overflow: hidden; position: relative; z-index: 1; }
.lc-left { flex: 0 0 340px; background: linear-gradient(135deg, #4361ee, #3730a3); padding: 48px 36px; color: #fff; display: flex; align-items: center; }
.brand { text-align: center; }
.brand-icon { color: rgba(255,255,255,.9); margin-bottom: 4px; }
.brand h1 { font-size: 24px; font-weight: 700; margin: 0 0 8px; }
.brand p { font-size: 14px; opacity: .8; margin: 0 0 36px; }
.features { text-align: left; display: flex; flex-direction: column; gap: 14px; }
.feat { display: flex; align-items: center; gap: 8px; font-size: 14px; opacity: .9; }
.lc-right { flex: 1; padding: 48px 44px; display: flex; flex-direction: column; justify-content: center; }
.lc-right h2 { font-size: 26px; font-weight: 700; margin: 0 0 4px; color: var(--text-primary); }
.sub { font-size: 14px; color: var(--text-muted); margin: 0 0 32px; }
.login-btn { width: 100%; height: 48px; font-size: 16px; font-weight: 600; letter-spacing: 4px; }
.toggle { text-align: center; font-size: 14px; color: var(--text-muted); margin-top: 16px; }
.toggle a { color: var(--primary); font-weight: 600; text-decoration: none; }
@media (max-width: 768px) { .login-card { flex-direction: column; max-width: 400px; } .lc-left { flex: 0 0 auto; padding: 32px 24px; } .features { display: none; } .lc-right { padding: 32px 24px; } }
</style>
