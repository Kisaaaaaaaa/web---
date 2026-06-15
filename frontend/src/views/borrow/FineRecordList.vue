<template>
  <div>
    <div class="bh-header">
      <h2>💰 我的罚金</h2>
      <el-tag type="danger" size="large" effect="dark">待缴：¥{{ unpaidTotal }}</el-tag>
    </div>

    <el-card shadow="never" class="card" v-if="list.length===0 && !loading">
      <el-empty description="暂无罚金记录，继续保持！" :image-size="120" />
    </el-card>

    <!-- 桌面表格 -->
    <div class="table-card hidden-sm" v-if="list.length>0">
      <el-table :data="list" v-loading="loading" stripe size="medium">
        <el-table-column label="书名" min-width="160">
          <template #default="{row}">
            <div class="cell-book">
              <div>
                <p class="cb-t">{{ row.bookTitle }}</p>
                <p class="cb-i">ISBN: {{ row.isbn }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="逾期天数" width="90">
          <template #default="{row}">{{ row.overdueDays }}天</template>
        </el-table-column>
        <el-table-column label="罚金" width="100">
          <template #default="{row}"><span class="fine">¥{{ row.fineAmount }}</span></template>
        </el-table-column>
        <el-table-column label="缴费状态" width="100">
          <template #default="{row}">
            <el-tag :type="row.paymentStatus===1?'success':'danger'" size="small">
              {{ row.paymentStatus===1?'已缴纳':'未缴纳' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="110">
          <template #default="{row}">{{ sd(row.createTime) }}</template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 移动端卡片 -->
    <div class="show-sm" v-if="list.length>0">
      <div v-for="r in list" :key="r.id" class="mc">
        <div class="mc-top">
          <strong>{{ r.bookTitle }}</strong>
          <el-tag :type="r.paymentStatus===1?'success':'danger'" size="small">
            {{ r.paymentStatus===1?'已缴纳':'未缴纳' }}
          </el-tag>
        </div>
        <p class="mc-p">逾期{{ r.overdueDays }}天 · <span class="fine">¥{{ r.fineAmount }}</span></p>
        <p class="mc-p" style="font-size:12px;color:#909399">{{ sd(r.createTime) }}</p>
      </div>
    </div>

    <div class="pg-wrap" v-if="total>q.pageSize">
      <el-pagination v-model:current-page="q.page" :page-size="q.pageSize" :total="total"
                     layout="prev,pager,next,total" @current-change="fetch" background />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getFineRecords, getUnpaidTotal } from '@/api/fine'

const loading = ref(false), list = ref([]), total = ref(0), unpaidTotal = ref('0.00')
const q = reactive({ page: 1, pageSize: 10 })

onMounted(() => { fetch(); loadTotal() })

async function fetch() {
  loading.value = true
  try {
    const r = await getFineRecords({ page: q.page, pageSize: q.pageSize })
    list.value = r.data.list; total.value = r.data.total
  } catch {} finally { loading.value = false }
}

async function loadTotal() {
  try { const r = await getUnpaidTotal(); unpaidTotal.value = Number(r.data||0).toFixed(2) } catch {}
}

function sd(t) { return t ? t.substring(0, 10) : '-' }
</script>

<style scoped>
.bh-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.bh-header h2 { margin: 0; font-size: 20px; font-weight: 700; }
.card { border-radius: 12px; }
.table-card { background: var(--surface); border-radius: var(--radius); box-shadow: var(--shadow); padding: 24px; }
.cell-book { display: flex; align-items: center; gap: 10px; }
.cb-t { font-weight: 600; font-size: 14px; margin: 0; }
.cb-i { font-size: 11px; color: var(--text-muted); font-family: monospace; margin: 2px 0 0; }
.fine { color: var(--danger); font-weight: 700; }
.pg-wrap { display: flex; justify-content: center; margin-top: 24px; }

.show-sm { display: none; }
.mc { background: var(--surface); border-radius: var(--radius); box-shadow: var(--shadow); padding: 16px; margin-bottom: 12px; }
.mc-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.mc-p { font-size: 13px; color: var(--text-secondary); margin: 4px 0; }
@media (max-width: 768px) { .hidden-sm { display: none; } .show-sm { display: block; } }
</style>
