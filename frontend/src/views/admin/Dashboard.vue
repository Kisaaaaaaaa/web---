<template>
  <div>
    <div class="dash-header"><h2>📊 数据仪表盘</h2><span class="dash-date">{{ today }}</span></div>
    <div class="stats-grid">
      <div class="stat-card blue"><div class="sn">{{ stats.total }}</div><div class="sl">总借阅量</div></div>
      <div class="stat-card green"><div class="sn">{{ stats.borrowing }}</div><div class="sl">借阅中</div></div>
      <div class="stat-card red"><div class="sn">{{ stats.overdue }}</div><div class="sl">逾期未还</div></div>
      <div class="stat-card purple"><div class="sn">{{ stats.returned }}</div><div class="sl">已归还</div></div>
    </div>
    <div class="charts-grid">
      <div class="chart-card"><div class="chart-title">📈 近7天借阅量</div><div ref="c1" style="height:290px"></div></div>
      <div class="chart-card"><div class="chart-title">📊 热门分类借阅</div><div ref="c2" style="height:290px"></div></div>
      <div class="chart-card"><div class="chart-title">⚠️ 逾期趋势（近7天）</div><div ref="c3" style="height:290px"></div></div>
      <div class="chart-card"><div class="chart-title">📋 借阅状态分布</div><div ref="c4" style="height:290px"></div></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'; import * as echarts from 'echarts'
import { getBorrowStats, getBorrowRecords } from '@/api/borrow'

const today = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
const stats = reactive({ borrowing: 0, returned: 0, overdue: 0, total: 0 })
const c1 = ref(null), c2 = ref(null), c3 = ref(null), c4 = ref(null)

onMounted(async () => {
  let rawStats = {}
  try { const r = await getBorrowStats(); rawStats = r.data; Object.assign(stats, { borrowing: rawStats.borrowing||0, returned: rawStats.returned||0, overdue: rawStats.overdue||0, total: rawStats.total||0 }) } catch {}

  const days = [], wb = [], ob = []
  const now = new Date()
  for (let i = 6; i >= 0; i--) { const d = new Date(now); d.setDate(d.getDate() - i); days.push(d.toISOString().substring(5, 10)); wb.push(0); ob.push(0) }
  const cat = {}

  try {
    const r = await getBorrowRecords({ page: 1, pageSize: 999 })
    ;(r.data?.list || []).forEach(item => {
      const dd = (item.borrowTime || '').substring(5, 10); const idx = days.indexOf(dd); if (idx >= 0) wb[idx]++
      if (item.status === 3) { const rd = (item.dueTime || '').substring(5, 10); const ri = days.indexOf(rd); if (ri >= 0) ob[ri]++ }
      const t = item.bookTitle || ''; let k = '其他'
      if (/计算机|编程|数据|算法|网络|软件/.test(t)) k = '计算机'
      else if (/文学|小说|诗|散文/.test(t)) k = '文学'
      else if (/科学|物理|化学|数学|生物/.test(t)) k = '自然科学'
      else if (/经济|管理|营销|财务/.test(t)) k = '经管'
      else if (/哲学|心理|思想/.test(t)) k = '哲学社科'
      cat[k] = (cat[k] || 0) + 1
    })
  } catch {}

  await nextTick()
  const cd = Object.entries(cat).map(([n, v]) => ({ name: n, value: v }))
  const initChart = (el, opt) => { if (el) { const e = echarts.init(el); e.setOption({ tooltip: { trigger: 'axis' }, grid: { left: 50, right: 20, top: 16, bottom: 30 }, ...opt }) } }

  initChart(c1.value, { xAxis: { data: days }, yAxis: { type: 'value' }, series: [{ data: wb, type: 'line', smooth: true, areaStyle: { opacity: .15 }, itemStyle: { color: '#4361ee' } }] })
  if (c2.value) { const e = echarts.init(c2.value); e.setOption({ tooltip: { trigger: 'item' }, legend: { orient: 'vertical', right: 8, top: 'center' }, series: [{ type: 'pie', radius: ['45%','75%'], center: ['42%','50%'], label: { show: false }, data: cd.length ? cd : [{ name: '暂无', value: 1 }] }] }) }
  initChart(c3.value, { xAxis: { data: days }, yAxis: { type: 'value' }, series: [{ data: ob, type: 'bar', itemStyle: { color: '#ef4444', borderRadius: [6,6,0,0] }, barMaxWidth: 28 }] })
  if (c4.value) { const e = echarts.init(c4.value); e.setOption({ tooltip: { trigger: 'item' }, series: [{ type: 'pie', radius: ['45%','75%'], center: ['50%','50%'], data: [{ name: '借阅中', value: stats.borrowing, itemStyle: { color: '#4361ee' } }, { name: '已归还', value: stats.returned, itemStyle: { color: '#10b981' } }, { name: '已逾期', value: stats.overdue, itemStyle: { color: '#ef4444' } }] }] }) }
})
</script>

<style scoped>
.dash-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.dash-header h2 { margin: 0; font-size: 22px; }
.dash-date { color: var(--text-muted); font-size: 14px; }

.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 16px; }
.stat-card { border-radius: var(--radius); padding: 22px 20px; color: #fff; }
.stat-card.blue { background: linear-gradient(135deg, #4361ee, #3a56d4); }
.stat-card.green { background: linear-gradient(135deg, #10b981, #059669); }
.stat-card.red { background: linear-gradient(135deg, #ef4444, #dc2626); }
.stat-card.purple { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }
.sn { font-size: 36px; font-weight: 700; line-height: 1.1; }
.sl { font-size: 13px; opacity: .85; margin-top: 4px; }

.charts-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.chart-card { background: var(--surface); border-radius: var(--radius); box-shadow: var(--shadow); padding: 20px; }
.chart-title { font-size: 15px; font-weight: 600; color: var(--text-primary); margin-bottom: 12px; }

@media (max-width: 1024px) { .charts-grid { grid-template-columns: 1fr; } .stats-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
