<template>
  <div>
    <div class="page-hd"><h2>用户管理</h2><span class="total-label">共 {{ total }} 人</span></div>
    <div class="filter-bar">
      <el-input v-model="query.username" placeholder="用户名" clearable style="width:150px" />
      <el-input v-model="query.realName" placeholder="姓名" clearable style="width:150px" />
      <el-select v-model="query.roleId" placeholder="角色" clearable style="width:130px"><el-option label="管理员" :value="1" /><el-option label="学生" :value="2" /></el-select>
      <el-button type="primary" @click="fetchUsers">搜索</el-button>
    </div>
    <div class="table-card">
      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="130" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="phone" label="手机" width="140" />
        <el-table-column label="角色" width="90"><template #default="{ row }"><el-tag :type="row.roleId===1?'danger':'primary'" size="small">{{ row.roleId===1?'管理员':'学生' }}</el-tag></template></el-table-column>
        <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'启用':'禁用' }}</el-tag></template></el-table-column>
        <el-table-column label="创建时间" width="170"><template #default="{ row }">{{ row.createTime?.replace('T',' ').substring(0,19) }}</template></el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }"><el-switch :model-value="row.status===1" inline-prompt active-text="启用" inactive-text="禁用" @change="v=>toggle(row,v?1:0)" /></template>
        </el-table-column>
      </el-table>
    </div>
    <div class="pg-wrap" v-if="total > query.pageSize"><el-pagination v-model:current-page="query.page" :page-size="query.pageSize" :total="total" layout="prev,pager,next,total" @current-change="fetchUsers" background /></div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'; import { ElMessage } from 'element-plus'
import { getUserList, updateUserStatus } from '@/api/user'
const loading = ref(false), users = ref([]), total = ref(0)
const query = reactive({ username: '', realName: '', roleId: null, page: 1, pageSize: 10 })

onMounted(() => fetchUsers())
async function fetchUsers() { loading.value = true; try { const r = await getUserList(query); users.value = r.data.list; total.value = r.data.total } catch {} finally { loading.value = false } }
async function toggle(row, status) { try { await updateUserStatus(row.id, status); row.status = status; ElMessage.success(status ? '已启用' : '已禁用') } catch {} }
</script>

<style scoped>
.page-hd { display: flex; align-items: baseline; gap: 12px; margin-bottom: 16px; }
.page-hd h2 { margin: 0; font-size: 20px; }
.total-label { color: var(--text-muted); font-size: 14px; }
.filter-bar { display: flex; gap: 12px; flex-wrap: wrap; align-items: center; margin-bottom: 16px; background: var(--surface); padding: 16px 20px; border-radius: var(--radius); box-shadow: var(--shadow); }
.table-card { background: var(--surface); border-radius: var(--radius); box-shadow: var(--shadow); padding: 20px 24px; }
.pg-wrap { display: flex; justify-content: center; margin-top: 20px; }
</style>
