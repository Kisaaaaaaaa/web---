<template>
  <div class="profile-page">
    <div class="profile-card">
      <div class="avatar-section">
        <el-upload :auto-upload="false" :show-file-list="false" accept="image/*" @change="onAvatarChange">
          <el-avatar :size="100" :src="userStore.avatarUrl" class="avatar" style="cursor:pointer">
            {{ userStore.avatarUrl ? '' : (userStore.realName?.charAt(0) || 'U') }}
          </el-avatar>
        </el-upload>
        <p class="avatar-hint">点击头像更换照片</p>
        <h2>{{ userStore.realName }}</h2>
        <el-tag :type="userStore.isAdmin ? 'danger' : 'primary'" effect="dark" round size="small">{{ userStore.isAdmin ? '系统管理员' : '学生' }}</el-tag>
      </div>

      <div class="info-section">
        <div class="info-row" v-for="row in infoRows" :key="row.label">
          <span class="info-label">{{ row.label }}</span>
          <span class="info-value">
            {{ row.value }}
            <el-tag v-if="row.locked" size="small" type="info" effect="plain" class="lock-tag">不可修改</el-tag>
          </span>
        </div>
      </div>

      <div class="actions">
        <el-button type="primary" size="large" @click="openEdit" class="action-btn">编辑个人信息</el-button>
        <el-button size="large" @click="pwdDlg = true" class="action-btn">修改密码</el-button>
      </div>
    </div>

    <el-dialog v-model="editDlg" title="编辑个人信息" width="440px" destroy-on-close top="8vh">
      <el-form :model="form" label-width="80px">
        <el-form-item label="手机号码"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="电子邮箱"><el-input v-model="form.email" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="editDlg = false" size="large">取消</el-button><el-button type="primary" :loading="saving" @click="doSave" size="large">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="pwdDlg" title="修改密码" width="420px" destroy-on-close top="8vh">
      <el-form :model="pwd" :rules="pwdRules" ref="pwdRef" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword"><el-input v-model="pwd.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码" prop="newPassword"><el-input v-model="pwd.newPassword" type="password" show-password /></el-form-item>
        <el-form-item label="确认密码" prop="rePassword"><el-input v-model="pwd.rePassword" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer><el-button @click="pwdDlg = false" size="large">取消</el-button><el-button type="primary" :loading="pwdSaving" @click="doChangePwd" size="large">确认</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'; import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'; import { uploadFile } from '@/api/review'; import request from '@/utils/request'

const userStore = useUserStore(); const userData = ref({})
const infoRows = computed(() => [
  { label: '用户名', value: userStore.userInfo?.username || '-', locked: true },
  { label: '真实姓名', value: userStore.realName || '-', locked: true },
  { label: '学号', value: userData.value.studentNo || '-', locked: true },
  { label: '手机号码', value: userData.value.phone || '-' },
  { label: '电子邮箱', value: userData.value.email || '-' },
  { label: '注册时间', value: userData.value.createTime ? userData.value.createTime.substring(0, 10) : '-' },
])
const editDlg = ref(false), saving = ref(false)
const form = reactive({ phone: '', email: '' })
const pwdDlg = ref(false), pwdSaving = ref(false), pwdRef = ref(null)
const pwd = reactive({ oldPassword: '', newPassword: '', rePassword: '' })
const pwdRules = { oldPassword: [{ required: true }], newPassword: [{ required: true, min: 6 }], rePassword: [{ required: true, validator: (_, v, cb) => v !== pwd.newPassword ? cb('两次不一致') : cb() }] }

onMounted(async () => { try { const r = await request({ url: '/users/me', method: 'get' }); userData.value = r.data; if (r.data.avatarUrl && !userStore.avatarUrl) userStore.setAvatar(r.data.avatarUrl) } catch {} })

async function onAvatarChange(file) { try { const r = await uploadFile(file.raw); await request({ url: '/users/profile/avatar', method: 'put', data: { avatarUrl: r.data.url } }); userStore.setAvatar(r.data.url); ElMessage.success('已更新') } catch {} }
function openEdit() { form.phone = userData.value.phone || ''; form.email = userData.value.email || ''; editDlg.value = true }
async function doSave() { saving.value = true; try { await request({ url: '/users/profile/info', method: 'put', data: form }); userData.value = { ...userData.value, phone: form.phone, email: form.email }; ElMessage.success('已保存'); editDlg.value = false } catch {} finally { saving.value = false } }
async function doChangePwd() { if (!await pwdRef.value.validate().catch(() => false)) return; pwdSaving.value = true; try { await request({ url: '/users/profile/password', method: 'put', data: { oldPassword: pwd.oldPassword, newPassword: pwd.newPassword } }); ElMessage.success('已修改'); pwdDlg.value = false } catch {} finally { pwdSaving.value = false } }
</script>

<style scoped>
.profile-page { max-width: 540px; margin: 0 auto; }
.profile-card { background: var(--surface); border-radius: var(--radius-lg); box-shadow: var(--shadow-lg); padding: 44px; }
.avatar-section { text-align: center; padding-bottom: 28px; border-bottom: 1px solid var(--border); }
.avatar { background: linear-gradient(135deg, var(--primary), var(--purple)); font-size: 36px; font-weight: 700; margin-bottom: 10px; }
.avatar-hint { margin: 0 0 12px; font-size: 12px; color: var(--text-muted); }
.avatar-section h2 { font-size: 22px; font-weight: 700; margin: 0 0 10px; }
.info-section { padding: 28px 0; }
.info-row { display: flex; justify-content: space-between; align-items: center; padding: 14px 0; border-bottom: 1px solid #f8fafc; }
.info-row:last-child { border-bottom: none; }
.info-label { font-size: 15px; color: var(--text-secondary); }
.info-value { font-size: 15px; color: var(--text-primary); font-weight: 500; display: flex; align-items: center; gap: 8px; }
.lock-tag { font-size: 11px; }
.actions { display: flex; flex-direction: column; gap: 10px; padding-top: 4px; }
.action-btn { width: 100%; margin-left: 0 !important; }
</style>
