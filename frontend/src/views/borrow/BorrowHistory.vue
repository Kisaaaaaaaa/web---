<template>
  <div>
    <div class="bh-header"><h2>我的借阅</h2>
      <el-select v-model="q.status" placeholder="全部状态" clearable style="width:130px" @change="fetch">
        <el-option label="借阅中" :value="0" /><el-option label="已续借" :value="1" /><el-option label="已归还" :value="2" /><el-option label="已逾期" :value="3" />
      </el-select>
    </div>

    <!-- 桌面表格 -->
    <div class="table-card hidden-sm">
      <div class="table-wrap">
        <el-table :data="list" v-loading="loading" stripe size="medium" empty-text="暂无借阅记录">
          <el-table-column label="书名" min-width="150"><template #default="{ row }"><div class="cell-book"><el-image :src="row.coverUrl" fit="cover" lazy class="mini-cover"><template #error><el-icon :size="16"><PictureFilled /></el-icon></template></el-image><div><p class="cb-t">{{ row.bookTitle }}</p><p class="cb-i">{{ row.isbn }}</p></div></div></template></el-table-column>
          <el-table-column label="借阅时间" width="105"><template #default="{ row }">{{ sd(row.borrowTime) }}</template></el-table-column>
          <el-table-column label="应还时间" width="105"><template #default="{ row }">{{ sd(row.dueTime) }}</template></el-table-column>
          <el-table-column label="归还" width="105"><template #default="{ row }">{{ row.returnTime ? sd(row.returnTime) : '—' }}</template></el-table-column>
          <el-table-column label="进度" width="140">
            <template #default="{ row }">
              <template v-if="row.status===0||row.status===1||row.status===3">
                <el-progress :percentage="calPct(row)" :color="calColor(row)" :stroke-width="7" :show-text="false" />
                <span :class="['pg-text', calDays(row)<0?'red':'']">{{ calDays(row) >= 0 ? `${calDays(row)}天后到期` : `已逾期${Math.abs(calDays(row))}天` }}</span>
              </template>
              <span v-else class="text-muted">已归还</span>
            </template>
          </el-table-column>
          <el-table-column label="罚金" width="90">
            <template #default="{ row }"><span v-if="row.overdueDays>0" class="fine">¥{{ row.dynamicFine }}</span><span v-else class="text-muted">无</span></template>
          </el-table-column>
          <el-table-column label="状态" width="80"><template #default="{ row }"><el-tag :type="['','success','info','danger'][row.status]" size="small">{{ ['借阅中','已续借','已归还','已逾期'][row.status] }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status===0||row.status===1" type="primary" size="small" @click="doRenew(row.id)">续借</el-button>
              <el-button v-if="row.status!==2" type="success" size="small" @click="doReturn(row.id)">归还</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 移动端卡片 -->
    <div class="show-sm">
      <div v-for="r in list" :key="r.id" class="mc">
        <div class="mc-top"><strong>{{ r.bookTitle }}</strong><el-tag :type="['','success','info','danger'][r.status]" size="small">{{['借阅中','已续借','已归还','已逾期'][r.status]}}</el-tag></div>
        <p class="mc-p">应还：{{ sd(r.dueTime) }}<template v-if="r.overdueDays>0"><span class="fine"> · 罚 ¥{{ r.dynamicFine }}</span></template></p>
        <div v-if="r.status!==2" style="margin:6px 0"><el-progress :percentage="calPct(r)" :color="calColor(r)" :stroke-width="7" /><span :class="['pg-text', calDays(r)<0?'red':'']">{{ calDays(r)>=0?`${calDays(r)}天到期`:`逾期${Math.abs(calDays(r))}天` }}</span></div>
        <div class="mc-act"><el-button v-if="r.status===0||r.status===1" size="small" type="primary" @click="doRenew(r.id)">续借</el-button><el-button v-if="r.status!==2" size="small" type="success" @click="doReturn(r.id)">归还</el-button></div>
      </div>
    </div>

    <div class="pg-wrap" v-if="total>q.pageSize"><el-pagination v-model:current-page="q.page" :page-size="q.pageSize" :total="total" layout="prev,pager,next,total" @current-change="fetch" background /></div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'; import { ElMessage, ElMessageBox } from 'element-plus'; import { PictureFilled } from '@element-plus/icons-vue'
import { getBorrowRecords, returnBook, renewBook } from '@/api/borrow'

const loading = ref(false), list = ref([]), total = ref(0)
const q = reactive({ status: null, page: 1, pageSize: 10 })

onMounted(() => fetch())
async function fetch() { loading.value = true; try { const r = await getBorrowRecords(q); list.value = r.data.list; total.value = r.data.total } catch {} finally { loading.value = false } }
async function doReturn(id) { try { await ElMessageBox.confirm('确认归还？', '归还', { type: 'info' }); await returnBook(id); ElMessage.success('已归还'); fetch() } catch {} }
async function doRenew(id) { try { await ElMessageBox.confirm('续借延长30天期限', '续借', { type: 'info' }); await renewBook(id); ElMessage.success('已续借'); fetch() } catch {} }

function sd(t) { return t?.substring(0, 10) || '-' }
function calPct(r) { const n = new Date(), b = new Date(r.borrowTime), d = new Date(r.dueTime); const t = d.getTime() - b.getTime(), e = n.getTime() - b.getTime(); return Math.min(100, Math.max(0, Math.round(e / t * 100))) }
function calDays(r) { if (!r?.dueTime) return 0; try { return Math.ceil((new Date(r.dueTime).getTime() - Date.now()) / 86400000) } catch { return 0 } }
function calColor(r) { const d = calDays(r); return d < 0 ? '#ef4444' : d < 3 ? '#f59e0b' : '#4361ee' }
</script>

<style scoped>
.bh-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.bh-header h2 { margin: 0; font-size: 20px; font-weight: 700; }
.table-card { background: var(--surface); border-radius: var(--radius); box-shadow: var(--shadow); padding: 24px; }
.table-wrap { overflow-x: auto; }
.cell-book { display: flex; align-items: center; gap: 10px; }
.mini-cover { width: 40px; height: 55px; border-radius: 4px; flex-shrink: 0; }
.cb-t { font-weight: 600; font-size: 14px; margin: 0; }
.cb-i { font-size: 11px; color: var(--text-muted); font-family: monospace; margin: 2px 0 0; }
.pg-text { font-size: 11px; font-weight: 600; display: block; margin-top: 2px; color: var(--primary); }
.pg-text.red { color: var(--danger); }
.fine { color: var(--danger); font-weight: 700; }
.text-muted { color: var(--text-muted); }
.pg-wrap { display: flex; justify-content: center; margin-top: 24px; }

.show-sm { display: none; }
.mc { background: var(--surface); border-radius: var(--radius); box-shadow: var(--shadow); padding: 16px; margin-bottom: 12px; }
.mc-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.mc-p { font-size: 13px; color: var(--text-secondary); margin: 4px 0; }
.mc-act { display: flex; gap: 8px; margin-top: 8px; }

@media (max-width: 768px) { .hidden-sm { display: none; } .show-sm { display: block; } }
</style>
