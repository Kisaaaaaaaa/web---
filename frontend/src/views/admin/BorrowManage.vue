<template>
  <div>
    <div class="page-hd"><h2>借阅管理</h2></div>

    <!-- 筛选栏：按书名 + 按用户 + 按状态 -->
    <el-card shadow="never" class="filter-card">
      <el-row :gutter="12" align="middle">
        <el-col :xs="24" :sm="7" :md="6">
          <el-input v-model="q.bookKeyword" placeholder="搜索书名或ISBN" clearable @keyup.enter="fetch" />
        </el-col>
        <el-col :xs="24" :sm="7" :md="6">
          <el-input v-model="q.username" placeholder="搜索用户名或真实姓名" clearable @keyup.enter="fetch" />
        </el-col>
        <el-col :xs="12" :sm="5" :md="3">
          <el-select v-model="q.status" placeholder="全部状态" clearable style="width:100%" @change="fetch">
            <el-option label="借阅中" :value="0" /><el-option label="已续借" :value="1" />
            <el-option label="已归还" :value="2" /><el-option label="已逾期" :value="3" />
          </el-select>
        </el-col>
        <el-col :xs="12" :sm="5" :md="3">
          <el-button type="primary" @click="fetch" style="width:100%">搜索</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 借阅记录明细 -->
    <el-card shadow="never" class="card">
      <template #header>📋 借阅记录明细</template>
      <div class="tbl-wrap">
        <el-table :data="list" v-loading="loading" stripe size="medium">
          <el-table-column label="借阅人" width="90"><template #default="{row}">{{ row.realName }}</template></el-table-column>
          <el-table-column label="书名" min-width="180" show-overflow-tooltip><template #default="{row}">{{ row.bookTitle }}</template></el-table-column>
          <el-table-column label="ISBN" width="140"><template #default="{row}"><span style="font-size:12px;color:#909399;font-family:monospace">{{ row.isbn }}</span></template></el-table-column>
          <el-table-column label="借阅时间" width="105"><template #default="{row}">{{ shortDate(row.borrowTime) }}</template></el-table-column>
          <el-table-column label="应还时间" width="105"><template #default="{row}">{{ shortDate(row.dueTime) }}</template></el-table-column>
          <el-table-column label="状态" width="80"><template #default="{row}"><el-tag :type="['','success','info','danger'][row.status]" size="small">{{ ['借阅中','已续借','已归还','已逾期'][row.status] }}</el-tag></template></el-table-column>
          <el-table-column label="逾期/罚金" width="100"><template #default="{row}"><span v-if="row.overdueDays>0" style="color:#f56c6c;font-weight:600">{{row.overdueDays}}天 / ¥{{row.dynamicFine}}</span><span v-else class="mu">-</span></template></el-table-column>
          <el-table-column label="操作" width="90" fixed="right"><template #default="{row}"><el-button v-if="row.status===0||row.status===1" type="warning" link size="small" @click="doOverdue(row.id)">标记逾期</el-button></template></el-table-column>
        </el-table>
      </div>
      <div v-if="total>q.pageSize" style="text-align:center;margin-top:16px"><el-pagination v-model:current-page="q.page" :page-size="q.pageSize" :total="total" layout="prev,pager,next,total" @current-change="fetch" background/></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getBorrowRecords, markOverdue } from '@/api/borrow'

const loading = ref(false), list = ref([]), total = ref(0)
const q = reactive({ username: '', bookKeyword: '', status: null, page: 1, pageSize: 12 })

onMounted(() => fetch())

async function fetch() {
  loading.value = true
  try {
    const params = { page: q.page, pageSize: q.pageSize }
    if (q.status != null) params.status = q.status
    // 用 username 参数搜用户名，此参数后端已支持模糊搜索 username 和 realName
    if (q.username) params.username = q.username
    if (q.bookKeyword) params.bookKeyword = q.bookKeyword
    const r = await getBorrowRecords(params)
    list.value = r.data.list
    total.value = r.data.total
  } catch {} finally { loading.value = false }
}

async function doOverdue(id) { try { await markOverdue(id); ElMessage.success('已标记逾期'); fetch() } catch {} }
function shortDate(t) { return t ? t.substring(0, 10) : '-' }
</script>

<style scoped>
.page-hd { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-hd h2 { margin: 0; font-size: 20px; }
.filter-card { border-radius: 12px; margin-bottom: 16px; }
.card { border-radius: 12px; }
.tbl-wrap { overflow-x: auto; }
.mu { color: #c0c4cc; }
</style>
