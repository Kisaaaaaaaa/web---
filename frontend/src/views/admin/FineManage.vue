<template>
  <div>
    <div class="page-hd">
      <h2>罚金管理</h2>
      <el-tag type="danger" size="large">未缴总额：¥{{ unpaidTotal }}</el-tag>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card red">
        <div class="sn">¥{{ unpaidTotal }}</div>
        <div class="sl">未缴纳罚金</div>
      </div>
      <div class="stat-card green">
        <div class="sn">{{ paidCount }}</div>
        <div class="sl">已缴纳笔数</div>
      </div>
      <div class="stat-card blue">
        <div class="sn">{{ unpaidCount }}</div>
        <div class="sl">未缴纳笔数</div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <el-card shadow="never" class="filter-card">
      <el-row :gutter="12" align="middle">
        <el-col :xs="24" :sm="8" :md="6">
          <el-input v-model="q.username" placeholder="搜索用户名或真实姓名" clearable @keyup.enter="fetch" />
        </el-col>
        <el-col :xs="12" :sm="5" :md="3">
          <el-select v-model="q.paymentStatus" placeholder="缴费状态" clearable style="width:100%" @change="fetch">
            <el-option label="未缴纳" :value="0" />
            <el-option label="已缴纳" :value="1" />
          </el-select>
        </el-col>
        <el-col :xs="12" :sm="5" :md="3">
          <el-button type="primary" @click="fetch" style="width:100%">搜索</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 罚金明细 -->
    <el-card shadow="never" class="card">
      <template #header>罚金明细</template>
      <div class="tbl-wrap">
        <el-table :data="list" v-loading="loading" stripe size="medium">
          <el-table-column label="借阅人" width="90">
            <template #default="{row}">{{ row.realName }}</template>
          </el-table-column>
          <el-table-column label="书名" min-width="160" show-overflow-tooltip>
            <template #default="{row}">{{ row.bookTitle }}</template>
          </el-table-column>
          <el-table-column label="ISBN" width="140">
            <template #default="{row}"><span style="font-size:12px;color:#909399;font-family:monospace">{{ row.isbn }}</span></template>
          </el-table-column>
          <el-table-column label="逾期天数" width="90">
            <template #default="{row}">{{ row.overdueDays }}天</template>
          </el-table-column>
          <el-table-column label="罚金金额" width="100">
            <template #default="{row}"><span style="color:#f56c6c;font-weight:700">¥{{ row.fineAmount }}</span></template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{row}">
              <el-tag :type="row.paymentStatus===1?'success':'danger'" size="small">{{ row.paymentStatus===1?'已缴纳':'未缴纳' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="105">
            <template #default="{row}">{{ shortDate(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{row}">
              <el-button v-if="row.paymentStatus===0" type="success" size="small" @click="handlePay(row.id)">标记已缴</el-button>
              <el-button v-if="row.paymentStatus===1" type="warning" size="small" @click="handleUnpay(row.id)">标记未缴</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-if="total>q.pageSize" style="text-align:center;margin-top:16px">
        <el-pagination v-model:current-page="q.page" :page-size="q.pageSize" :total="total"
                       layout="prev,pager,next,total" @current-change="fetch" background />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getFineRecords, updatePaymentStatus, getUnpaidTotal } from '@/api/fine'

const loading = ref(false), list = ref([]), total = ref(0), unpaidTotal = ref('0.00')
const q = reactive({ username: '', paymentStatus: null, page: 1, pageSize: 12 })

const unpaidCount = computed(() => list.value.filter(i => i.paymentStatus === 0).length)
const paidCount = computed(() => list.value.filter(i => i.paymentStatus === 1).length)

onMounted(() => { fetch(); loadUnpaidTotal() })

async function fetch() {
  loading.value = true
  try {
    const params = { page: q.page, pageSize: q.pageSize }
    if (q.paymentStatus != null) params.paymentStatus = q.paymentStatus
    if (q.username) params.username = q.username
    const r = await getFineRecords(params)
    list.value = r.data.list; total.value = r.data.total
  } catch {} finally { loading.value = false }
}

async function loadUnpaidTotal() {
  try { const r = await getUnpaidTotal(); unpaidTotal.value = Number(r.data || 0).toFixed(2) } catch {}
}

async function handlePay(id) {
  try { await updatePaymentStatus(id, 1); ElMessage.success('已标记为已缴纳'); fetch(); loadUnpaidTotal() } catch {}
}
async function handleUnpay(id) {
  try { await updatePaymentStatus(id, 0); ElMessage.success('已标记为未缴纳'); fetch(); loadUnpaidTotal() } catch {}
}
function shortDate(t) { return t ? t.substring(0, 10) : '-' }
</script>

<style scoped>
.page-hd { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-hd h2 { margin: 0; font-size: 20px; font-weight: 700; }

.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 16px; }
.stat-card { border-radius: 12px; padding: 18px 20px; color: #fff; }
.stat-card.red { background: linear-gradient(135deg, #ef4444, #dc2626); }
.stat-card.green { background: linear-gradient(135deg, #10b981, #059669); }
.stat-card.blue { background: linear-gradient(135deg, #4361ee, #3a56d4); }
.sn { font-size: 28px; font-weight: 700; line-height: 1.1; }
.sl { font-size: 13px; opacity: .85; margin-top: 2px; }

.filter-card { border-radius: 12px; margin-bottom: 16px; }
.card { border-radius: 12px; }
.tbl-wrap { overflow-x: auto; }
@media (max-width: 768px) { .stats-grid { grid-template-columns: 1fr; } }
</style>
