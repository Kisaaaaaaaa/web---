<template>
  <div class="reg-page">
    <div class="reg-card">
      <h2>创建新账号</h2>
      <p class="sub">注册校园图书借阅系统</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="用户名" prop="username"><el-input v-model="form.username" placeholder="3-50位" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="真实姓名" prop="realName"><el-input v-model="form.realName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" show-password placeholder="不少于6位" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="确认密码" prop="rePassword"><el-input v-model="form.rePassword" type="password" show-password /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="学号" prop="studentNo"><el-input v-model="form.studentNo" placeholder="必填" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="手机号"><el-input v-model="form.phone" placeholder="选填" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="邮箱"><el-input v-model="form.email" placeholder="选填" /></el-form-item>
        <el-button type="primary" :loading="loading" class="reg-btn" @click="handleRegister">{{ loading ? '注册中...' : '注 册' }}</el-button>
      </el-form>
      <p class="toggle">已有账号？<router-link to="/login">去登录</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'; import { useRouter } from 'vue-router'; import { ElMessage } from 'element-plus'; import { useUserStore } from '@/stores/user'
const router = useRouter(); const userStore = useUserStore()
const formRef = ref(null); const loading = ref(false)
const form = reactive({ username: '', password: '', rePassword: '', realName: '', studentNo: '', phone: '', email: '' })
const rules = {
  username: [{ required: true }, { min: 3, max: 50 }],
  password: [{ required: true, min: 6 }],
  rePassword: [{ required: true, validator: (_, v, cb) => v !== form.password ? cb('两次密码不一致') : cb() }],
  realName: [{ required: true }],
  studentNo: [{ required: true, message: '请输入学号' }]
}

async function handleRegister() {
  if (!await formRef.value.validate().catch(() => false)) return; loading.value = true
  const { rePassword, ...data } = form; data.roleId = 2
  try { await userStore.register(data); ElMessage.success('注册成功'); router.push('/login') } catch {} finally { loading.value = false }
}
</script>

<style scoped>
/*
 * 背景图片路径：请将图片放入 frontend/public/ 目录下
 * 然后修改下方 url 为实际文件名，例如 url('/bg-register.jpg')
 */
.reg-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: url('/bg-register.jpg') center/cover no-repeat;
  background-color: #1e293b;
  position: relative; padding: 24px;
}
.reg-card { width: 100%; max-width: 560px; background: #fff; border-radius: var(--radius-xl); box-shadow: 0 25px 80px rgba(0,0,0,.25); padding: 44px 40px; position: relative; z-index: 1; }
.reg-card h2 { font-size: 24px; font-weight: 700; margin: 0 0 4px; }
.sub { font-size: 14px; color: var(--text-muted); margin: 0 0 28px; }
.reg-btn { width: 100%; height: 48px; font-size: 16px; font-weight: 600; letter-spacing: 4px; }
.toggle { text-align: center; font-size: 14px; color: var(--text-muted); margin-top: 12px; }
.toggle a { color: var(--primary); font-weight: 600; text-decoration: none; }
</style>
