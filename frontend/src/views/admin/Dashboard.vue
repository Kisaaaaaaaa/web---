<template>
  <div>
    <div class="dash-header"><h2>数据仪表盘</h2><span class="dash-date">{{ today }}</span></div>
    <div class="stats-grid">
      <div class="stat-card">
        <div class="sc-accent blue"></div>
        <div class="sc-body"><div class="sc-num">{{ stats.total }}</div><div class="sc-label">总借阅量</div></div>
        <div class="sc-icon-wrap blue"><el-icon :size="22"><Document /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="sc-accent green"></div>
        <div class="sc-body"><div class="sc-num">{{ stats.borrowing }}</div><div class="sc-label">借阅中</div></div>
        <div class="sc-icon-wrap green"><el-icon :size="22"><Notebook /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="sc-accent red"></div>
        <div class="sc-body"><div class="sc-num">{{ stats.overdue }}</div><div class="sc-label">逾期未还</div></div>
        <div class="sc-icon-wrap red"><el-icon :size="22"><WarningFilled /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="sc-accent purple"></div>
        <div class="sc-body"><div class="sc-num">{{ stats.returned }}</div><div class="sc-label">已归还</div></div>
        <div class="sc-icon-wrap purple"><el-icon :size="22"><CircleCheckFilled /></el-icon></div>
      </div>
    </div>
    <div class="charts-grid">
      <div class="chart-card"><div class="chart-title">近7天借阅量</div><div ref="c1" style="height:290px"></div></div>
      <div class="chart-card"><div class="chart-title">热门分类借阅</div><div ref="c2" style="height:290px"></div></div>
      <div class="chart-card"><div class="chart-title">逾期趋势（近7天）</div><div ref="c3" style="height:290px"></div></div>
      <div class="chart-card"><div class="chart-title">借阅状态分布</div><div ref="c4" style="height:290px"></div></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'; import * as echarts from 'echarts'
import { Document, Notebook, WarningFilled, CircleCheckFilled } from '@element-plus/icons-vue'
import { getBorrowStats, getStatsByCategory, getDailyStats, getStatusDistribution } from '@/api/borrow'

const today = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
const stats = reactive({ borrowing: 0, returned: 0, overdue: 0, total: 0 })
const c1 = ref(null), c2 = ref(null), c3 = ref(null), c4 = ref(null)

onMounted(async () => {
  // 总览统计
  try { const r = await getBorrowStats(); const d = r.data; Object.assign(stats, { borrowing: d.borrowing||0, returned: d.returned||0, overdue: d.overdue||0, total: d.total||0 }) } catch {}

  // 近7天趋势（SQL聚合查询）
  let days = [], wb = [], ob = []
  try {
    const r = await getDailyStats()
    ;(r.data || []).forEach(item => { days.push(item.day); wb.push(Number(item.borrow_count)||0); ob.push(Number(item.overdue_count)||0) })
  } catch {}
  if (!days.length) { const n = new Date(); for (let i = 6; i >= 0; i--) { const d = new Date(n); d.setDate(d.getDate() - i); days.push(d.toISOString().substring(5, 10)); wb.push(0); ob.push(0) } }

  // 分类借阅统计（SQL GROUP BY）
  let catData = [{ name: '暂无', value: 1 }]
  try {
    const r = await getStatsByCategory()
    catData = (r.data || []).map(({ name, value }) => ({ name, value: Number(value) || 0 }))
    if (!catData.length) catData = [{ name: '暂无', value: 1 }]
  } catch {}

  // 状态分布（SQL GROUP BY）
  let stData = [{ name: '借阅中', value: stats.borrowing, itemStyle: { color: '#4361ee' } }, { name: '已归还', value: stats.returned, itemStyle: { color: '#10b981' } }, { name: '已逾期', value: stats.overdue, itemStyle: { color: '#ef4444' } }]
  try {
    const r = await getStatusDistribution()
    stData = (r.data || []).map(({ name, value }) => {
      let color = '#4361ee'
      if (name === '已归还') color = '#10b981'
      else if (name === '已逾期') color = '#ef4444'
      else if (name === '已续借') color = '#f59e0b'
      return { name, value: Number(value) || 0, itemStyle: { color } }
    })
    if (!stData.length) stData = [{ name: '暂无', value: 1 }]
  } catch {}

  await nextTick()
  const initChart = (el, opt) => { if (el) { const e = echarts.init(el); e.setOption({ tooltip: { trigger: 'axis' }, grid: { left: 50, right: 20, top: 16, bottom: 30 }, ...opt }) } }

  initChart(c1.value, { xAxis: { data: days }, yAxis: { type: 'value' }, series: [{ data: wb, type: 'line', smooth: true, areaStyle: { opacity: .15 }, itemStyle: { color: '#4361ee' } }] })
  if (c2.value) { const e = echarts.init(c2.value); e.setOption({ tooltip: { trigger: 'item' }, legend: { orient: 'vertical', right: 8, top: 'center' }, series: [{ type: 'pie', radius: ['45%','75%'], center: ['42%','50%'], label: { show: false }, data: catData }] }) }
  initChart(c3.value, { xAxis: { data: days }, yAxis: { type: 'value' }, series: [{ data: ob, type: 'bar', itemStyle: { color: '#ef4444', borderRadius: [6,6,0,0] }, barMaxWidth: 28 }] })
  if (c4.value) { const e = echarts.init(c4.value); e.setOption({ tooltip: { trigger: 'item' }, series: [{ type: 'pie', radius: ['45%','75%'], center: ['50%','50%'], data: stData }] }) }
})
</script>

<style scoped>
.dash-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.dash-header h2 { margin: 0; font-size: 22px; }
.dash-date { color: var(--text-muted); font-size: 14px; }

.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 16px; }
.stat-card {
  background: var(--surface); border-radius: var(--radius); box-shadow: var(--shadow);
  padding: 20px 24px; display: flex; align-items: center; gap: 16px;
  transition: all .25s; cursor: default;
}
.stat-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-lg); }
.sc-accent {
  width: 4px; height: 48px; border-radius: 2px; flex-shrink: 0;
}
.sc-accent.blue   { background: #4361ee; }
.sc-accent.green  { background: #10b981; }
.sc-accent.red    { background: #ef4444; }
.sc-accent.purple { background: #8b5cf6; }
.sc-body { flex: 1; }
.sc-num { font-size: 32px; font-weight: 700; line-height: 1.1; color: var(--text-primary); }
.sc-label { font-size: 13px; color: var(--text-muted); margin-top: 2px; }
.sc-icon-wrap {
  width: 44px; height: 44px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.sc-icon-wrap.blue   { background: #eef2ff; color: #4361ee; }
.sc-icon-wrap.green  { background: #ecfdf5; color: #10b981; }
.sc-icon-wrap.red    { background: #fef2f2; color: #ef4444; }
.sc-icon-wrap.purple { background: #f5f3ff; color: #8b5cf6; }

.charts-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.chart-card { background: var(--surface); border-radius: var(--radius); box-shadow: var(--shadow); padding: 20px; }
.chart-title { font-size: 15px; font-weight: 600; color: var(--text-primary); margin-bottom: 12px; }

@media (max-width: 1024px) { .charts-grid { grid-template-columns: 1fr; } .stats-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
