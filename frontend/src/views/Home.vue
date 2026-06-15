<template>
  <div class="home-page">
    <!-- 轮播图 -->
    <div class="caro-wrap">
      <el-carousel v-if="carousels.length" height="340px" :interval="4000" arrow="hover">
        <el-carousel-item v-for="item in carousels" :key="item.id">
          <div class="caro-slide" @click="goCarousel(item)">
            <el-image :src="item.imageUrl" fit="cover" style="width:100%;height:100%;pointer-events:none" />
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 滚动公告条 -->
    <div class="anno-bar" v-if="announcements.length">
      <span class="anno-badge">📢 公告</span>
      <div class="anno-scroll">
        <span v-for="a in announcements" :key="a.id" class="anno-link" @click="openAnno(a)">
          <span v-if="a.isTop" class="anno-top">顶</span>
          {{ a.title }}：{{ a.content.substring(0, 40) }}{{ a.content.length > 40 ? '...' : '' }}
        </span>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div v-for="c in statCards" :key="c.label" :class="['stat-card', c.cls]" @click="$router.push(c.link)">
        <div class="sc-left">
          <div class="sc-num">{{ c.val }}</div>
          <div class="sc-label">{{ c.label }}</div>
        </div>
        <div class="sc-icon">
          <el-icon :size="28"><component :is="c.icon" /></el-icon>
        </div>
      </div>
    </div>

    <!-- 三列卡片 -->
    <div class="triple-grid">
      <!-- 热门借阅 TOP3 -->
      <div class="triple-card">
        <div class="tc-header">
          <span class="tc-title">🔥 热门借阅 TOP3</span>
          <el-button text size="small" type="primary" @click="$router.push('/books')">全部 →</el-button>
        </div>
        <div v-loading="rl" class="tc-body">
          <div v-for="(item, i) in topBooks" :key="item.bookId" class="top-row" @click="$router.push('/books/' + item.bookId)">
            <span :class="['top-rank', 'rank-' + (i < 3 ? i : 'n')]">{{ i + 1 }}</span>
            <el-image :src="item.coverUrl" fit="cover" lazy class="top-cover">
              <template #error><div class="cover-fb"><el-icon :size="16"><PictureFilled /></el-icon></div></template>
            </el-image>
            <div class="top-info">
              <p class="top-name">{{ item.title }}</p>
              <p class="top-author">{{ item.author }}</p>
            </div>
          </div>
          <el-empty v-if="!topBooks.length" description="暂无数据" :image-size="48" />
        </div>
      </div>

      <!-- 系统公告 -->
      <div class="triple-card">
        <div class="tc-header"><span class="tc-title">📢 系统公告</span></div>
        <div class="tc-body">
          <div v-for="a in announcements.slice(0, 5)" :key="a.id" class="anno-card" @click="openAnno(a)">
            <div class="ac-head">
              <span v-if="a.isTop" class="anno-top-sm">置顶</span>
              <span class="ac-title">{{ a.title }}</span>
            </div>
            <p class="ac-desc">{{ a.content.substring(0, 36) }}{{ a.content.length > 36 ? '...' : '' }}</p>
          </div>
          <el-empty v-if="!announcements.length" description="暂无公告" :image-size="48" />
        </div>
      </div>

      <!-- 借阅须知 -->
      <div class="triple-card">
        <div class="tc-header"><span class="tc-title">📋 借阅须知</span></div>
        <div class="tc-body">
          <div class="rule-item"><span class="ri-icon">📅</span><div><strong>借阅期限 30 天</strong><p>从借阅当天开始计算</p></div></div>
          <div class="rule-item"><span class="ri-icon">🔄</span><div><strong>可续借 1 次</strong><p>续借延长 30 天归还期限</p></div></div>
          <div class="rule-item"><span class="ri-icon">📚</span><div><strong>最多借阅 5 本</strong><p>同时借阅不超过 5 本图书</p></div></div>
          <div class="rule-item"><span class="ri-icon">💰</span><div><strong>逾期罚金 0.5 元/天</strong><p>请按时归还避免产生罚金</p></div></div>
        </div>
      </div>
    </div>

    <!-- 公告详情 -->
    <el-dialog v-model="annoDlg" :title="curAnno?.title" width="520px" top="10vh">
      <div v-if="curAnno" style="white-space:pre-wrap;line-height:1.8">{{ curAnno.content }}</div>
      <template #footer><el-button @click="annoDlg = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Notebook, CircleCheckFilled, Reading, Clock, PictureFilled } from '@element-plus/icons-vue'
import { getBorrowStats, getTopBooks } from '@/api/borrow'
import { getCarousels } from '@/api/carousel'
import { getAnnouncements } from '@/api/announcement'

const router = useRouter()
const carousels = ref([]), announcements = ref([]), topBooks = ref([])
const stats = ref({ borrowing: 0, returned: 0, overdue: 0, total: 0 }), rl = ref(false)
const annoDlg = ref(false), curAnno = ref(null)

const statCards = computed(() => [
  { label: '借阅中', val: stats.value.borrowing, icon: Notebook, cls: 'blue' },
  { label: '已逾期', val: stats.value.overdue, icon: Clock, cls: 'red' },
  { label: '已归还', val: stats.value.returned, icon: CircleCheckFilled, cls: 'green' },
  { label: '总借阅', val: stats.value.total, icon: Reading, cls: 'purple' },
])

function openAnno(a) { curAnno.value = a; annoDlg.value = true }
function goCarousel(item) {
  if (!item.linkUrl) return
  if (item.linkUrl.startsWith('http')) window.open(item.linkUrl, '_blank')
  else router.push(item.linkUrl)
}

onMounted(async () => {
  try { carousels.value = (await getCarousels()).data } catch {}
  try { announcements.value = (await getAnnouncements()).data } catch {}
  try { stats.value = (await getBorrowStats()).data } catch {}
  rl.value = true; try { topBooks.value = (await getTopBooks(3)).data } catch {} finally { rl.value = false }
})
</script>

<style scoped>
.caro-wrap { border-radius: var(--radius-lg); overflow: hidden; margin-bottom: 16px; box-shadow: var(--shadow-lg); }
.caro-slide { width: 100%; height: 340px; cursor: pointer; }

/* 公告滚动条 */
.anno-bar { display: flex; align-items: center; gap: 12px; background: var(--warning-light); border: 1px solid #fde68a; border-radius: var(--radius); padding: 10px 16px; margin-bottom: 16px; overflow: hidden; }
.anno-badge { font-size: 13px; font-weight: 600; white-space: nowrap; flex-shrink: 0; color: #92400e; }
.anno-scroll { display: flex; gap: 32px; white-space: nowrap; overflow-x: auto; scrollbar-width: none; }
.anno-scroll::-webkit-scrollbar { display: none; }
.anno-link { cursor: pointer; font-size: 13px; color: var(--text-secondary); display: inline-flex; align-items: center; gap: 6px; }
.anno-link:hover { color: var(--primary); }
.anno-top { background: var(--danger); color: #fff; font-size: 10px; padding: 1px 5px; border-radius: 3px; font-weight: 700; }

/* 统计卡片 */
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 16px; }
.stat-card { border-radius: var(--radius); padding: 20px 24px; color: #fff; display: flex; justify-content: space-between; align-items: center; cursor: pointer; transition: all .25s; }
.stat-card:hover { transform: translateY(-3px); box-shadow: 0 8px 24px rgba(0,0,0,.15); }
.stat-card.blue { background: linear-gradient(135deg, #4361ee, #3a56d4); }
.stat-card.red { background: linear-gradient(135deg, #ef4444, #dc2626); }
.stat-card.green { background: linear-gradient(135deg, #10b981, #059669); }
.stat-card.purple { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }
.sc-num { font-size: 34px; font-weight: 700; line-height: 1.1; }
.sc-label { font-size: 13px; opacity: .85; margin-top: 4px; }
.sc-icon { opacity: .25; font-size: 40px; }

/* 三列网格 */
.triple-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.triple-card { background: var(--surface); border-radius: var(--radius); box-shadow: var(--shadow); overflow: hidden; }
.tc-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 1px solid var(--border); }
.tc-title { font-size: 15px; font-weight: 700; color: var(--text-primary); }
.tc-body { padding: 12px 20px; min-height: 200px; }

/* TOP3 行 */
.top-row { display: flex; align-items: center; gap: 12px; padding: 10px 8px; border-radius: var(--radius-sm); cursor: pointer; transition: all .15s; }
.top-row:hover { background: var(--primary-light); }
.top-rank { width: 26px; height: 26px; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 13px; background: #e2e8f0; color: var(--text-muted); flex-shrink: 0; }
.rank-0 { background: #fbbf24; color: #fff; }
.rank-1 { background: #94a3b8; color: #fff; }
.rank-2 { background: #f59e0b; color: #fff; }
.top-cover { width: 34px; height: 48px; border-radius: 4px; flex-shrink: 0; }
.cover-fb { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: #f1f5f9; color: #cbd5e1; }
.top-info { flex: 1; min-width: 0; }
.top-name { font-size: 14px; font-weight: 600; margin: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.top-author { font-size: 12px; color: var(--text-muted); margin: 2px 0 0; }

/* 公告卡片 */
.anno-card { padding: 12px 0; border-bottom: 1px solid #f8fafc; cursor: pointer; transition: all .15s; }
.anno-card:last-child { border-bottom: none; }
.anno-card:hover .ac-title { color: var(--primary); }
.ac-head { display: flex; align-items: center; gap: 6px; margin-bottom: 4px; }
.anno-top-sm { background: var(--danger); color: #fff; font-size: 10px; padding: 1px 6px; border-radius: 3px; font-weight: 600; }
.ac-title { font-size: 14px; font-weight: 600; color: var(--text-primary); transition: color .15s; }
.ac-desc { margin: 0; font-size: 12px; color: var(--text-muted); }

/* 规则项 */
.rule-item { display: flex; align-items: flex-start; gap: 12px; padding: 14px 0; border-bottom: 1px solid #f8fafc; }
.rule-item:last-child { border-bottom: none; }
.ri-icon { font-size: 22px; flex-shrink: 0; margin-top: 2px; }
.rule-item strong { display: block; font-size: 14px; color: var(--text-primary); }
.rule-item p { margin: 2px 0 0; font-size: 12px; color: var(--text-muted); }

@media (max-width: 1024px) { .triple-grid { grid-template-columns: repeat(2, 1fr); } .stats-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) {
  .triple-grid { grid-template-columns: 1fr; }
  .caro-wrap .el-carousel { height: 220px; }
  .caro-slide { height: 220px; }
  .sc-num { font-size: 26px; }
}
</style>
