<template>
  <div>
    <!-- 搜索区 -->
    <div class="search-section">
      <div class="search-box">
        <el-input v-model="q.keyword" placeholder="搜索书名、作者或 ISBN..." clearable size="large" class="search-input" @keyup.enter="search" @clear="search">
          <template #prefix><el-icon :size="20"><Search /></el-icon></template>
        </el-input>
        <el-select v-model="q.categoryId" placeholder="全部分类" clearable size="large" style="width: 170px" @change="search">
          <el-option v-for="c in cats" :key="c.id" :label="c.categoryName" :value="c.id" />
        </el-select>
        <el-button type="primary" size="large" @click="search"><el-icon><Search /></el-icon> 搜索</el-button>
      </div>
      <div class="cat-tags">
        <span v-for="c in topCats" :key="c.id"
          :class="['cat-chip', { active: q.categoryId === c.id }]"
          @click="q.categoryId = q.categoryId === c.id ? null : c.id; search()">
          {{ c.categoryName }}
        </span>
      </div>
    </div>

    <!-- 结果计数 -->
    <div class="result-bar" v-if="!loading">共找到 <strong>{{ total }}</strong> 本图书</div>

    <!-- 图书网格 -->
    <div class="book-grid" v-loading="loading" element-loading-text="正在搜索图书...">
      <div v-for="b in books" :key="b.id" class="book-card" @click="$router.push('/books/' + b.id)">
        <div class="bc-cover">
          <el-image :src="b.coverUrl" fit="cover" lazy style="width:100%;height:100%">
            <template #error><div class="bc-no-cover"><el-icon :size="32"><PictureFilled /></el-icon></div></template>
          </el-image>
          <span v-if="b.currentStock === 0" class="bc-badge sold">借完</span>
          <span v-else-if="b.currentStock <= 3" class="bc-badge low">仅剩 {{ b.currentStock }} 本</span>
        </div>
        <div class="bc-info">
          <p class="bc-title">{{ b.title }}</p>
          <p class="bc-author">{{ b.author }}</p>
          <div class="bc-meta">
            <span class="bc-cat">{{ b.categoryName }}</span>
            <span :class="['bc-stock', { 'stock-zero': b.currentStock === 0, 'stock-low': b.currentStock > 0 && b.currentStock <= 3 }]">
              {{ b.currentStock > 0 ? `可借 ${b.currentStock} 本` : '暂无库存' }}
            </span>
          </div>
        </div>
      </div>
      <div v-if="!loading && books.length === 0" class="empty-wrap"><el-empty description="未找到匹配的图书" /></div>
    </div>

    <div class="pagination-wrap" v-if="total > q.pageSize">
      <el-pagination v-model:current-page="q.page" :page-size="q.pageSize" :total="total" layout="prev, pager, next, jumper, total" @current-change="fetch" background />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, PictureFilled } from '@element-plus/icons-vue'
import { getBookList } from '@/api/book'
import { getAllCategories } from '@/api/category'

const loading = ref(false), books = ref([]), total = ref(0), cats = ref([]), topCats = ref([])
const q = reactive({ keyword: '', categoryId: null, status: 1, page: 1, pageSize: 24 })

onMounted(async () => {
  try { const r = await getAllCategories(); cats.value = r.data; topCats.value = r.data.filter(c => c.parentId === 0) } catch {}
  fetch()
})

async function fetch() {
  loading.value = true
  try { const r = await getBookList({ keyword: q.keyword || undefined, categoryId: q.categoryId, status: q.status, page: q.page, pageSize: q.pageSize }); books.value = r.data.list; total.value = r.data.total } catch {} finally { loading.value = false }
}
function search() { q.page = 1; fetch() }
</script>

<style scoped>
.search-section { margin-bottom: 20px; }
.search-box { display: flex; gap: 12px; margin-bottom: 14px; }
.search-input { flex: 1; }
.cat-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.cat-chip {
  padding: 7px 18px; border-radius: 20px; font-size: 13px; font-weight: 500;
  background: var(--surface); color: var(--text-secondary);
  border: 1px solid var(--border); cursor: pointer;
  transition: all .2s; user-select: none;
}
.cat-chip:hover { border-color: var(--primary); color: var(--primary); }
.cat-chip.active { background: var(--primary-light); color: var(--primary); border-color: var(--primary); font-weight: 600; }

.result-bar { margin-bottom: 16px; font-size: 14px; color: var(--text-muted); }
.result-bar strong { color: var(--text-primary); }

.book-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 16px; min-height: 300px; }
.book-card {
  background: var(--surface); border-radius: var(--radius); overflow: hidden;
  box-shadow: var(--shadow-sm); cursor: pointer;
  transition: all .25s;
}
.book-card:hover { box-shadow: var(--shadow-lg); transform: translateY(-4px); }
.bc-cover { position: relative; width: 100%; aspect-ratio: 3/4; background: #f1f5f9; overflow: hidden; }
.bc-no-cover { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #cbd5e1; }
.bc-badge { position: absolute; top: 0; right: 0; padding: 5px 10px; font-size: 11px; font-weight: 700; color: #fff; border-radius: 0 0 0 8px; }
.bc-badge.sold { background: var(--danger); }
.bc-badge.low { background: var(--warning); }
.bc-info { padding: 10px 12px 12px; }
.bc-title { font-size: 14px; font-weight: 600; margin: 0 0 3px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: var(--text-primary); }
.bc-author { font-size: 12px; color: var(--text-muted); margin: 0 0 10px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.bc-meta { display: flex; justify-content: space-between; align-items: center; font-size: 11px; }
.bc-cat { color: var(--text-muted); }
.bc-stock { color: var(--success); font-weight: 600; }
.bc-stock.stock-low { color: var(--warning); }
.bc-stock.stock-zero { color: var(--danger); }

.empty-wrap { grid-column: 1 / -1; padding: 60px 0; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 28px; }

@media (max-width: 1400px) { .book-grid { grid-template-columns: repeat(5, 1fr); } }
@media (max-width: 1100px) { .book-grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 768px) {
  .book-grid { grid-template-columns: repeat(2, 1fr); gap: 10px; }
  .search-box { flex-wrap: wrap; }
  .search-input { width: 100%; }
}
</style>
