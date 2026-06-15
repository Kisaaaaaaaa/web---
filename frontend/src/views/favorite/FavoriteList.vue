<template>
  <div>
    <div class="fav-header"><h2>⭐ 我的收藏</h2><span class="fav-count">共 {{ favorites.length }} 本</span></div>

    <div class="book-grid" v-loading="loading">
      <div v-for="book in favorites" :key="book.id" class="book-card" @click="$router.push('/books/' + book.id)">
        <div class="bc-cover">
          <el-image :src="book.coverUrl" fit="cover" lazy style="width:100%;height:100%">
            <template #error><div class="bc-no-cover"><el-icon :size="32"><PictureFilled /></el-icon></div></template>
          </el-image>
          <span v-if="book.currentStock===0" class="bc-badge sold">借完</span>
          <span v-else-if="book.currentStock<=3" class="bc-badge low">仅剩{{book.currentStock}}本</span>
        </div>
        <div class="bc-info">
          <p class="bc-title">{{ book.title }}</p>
          <p class="bc-author">{{ book.author }}</p>
          <span :class="['bc-stock', book.currentStock===0?'zero':book.currentStock<=3?'low':'']">{{ book.currentStock>0?`可借${book.currentStock}本`:'暂无库存' }}</span>
        </div>
      </div>
      <div v-if="!loading&&favorites.length===0" class="empty-wrap">
        <el-empty description="还没有收藏任何图书"><el-button type="primary" @click="$router.push('/books')">去逛逛</el-button></el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'; import { PictureFilled } from '@element-plus/icons-vue'
import { getFavorites } from '@/api/favorite'

const loading = ref(false), favorites = ref([])

onMounted(async () => { loading.value = true; try { favorites.value = (await getFavorites()).data } catch {} finally { loading.value = false } })
</script>

<style scoped>
.fav-header { display: flex; align-items: baseline; gap: 12px; margin-bottom: 20px; }
.fav-header h2 { margin: 0; font-size: 20px; font-weight: 700; }
.fav-count { color: var(--text-muted); font-size: 14px; }

.book-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 16px; min-height: 200px; }
.book-card { background: var(--surface); border-radius: var(--radius); overflow: hidden; box-shadow: var(--shadow-sm); cursor: pointer; transition: all .25s; }
.book-card:hover { box-shadow: var(--shadow-lg); transform: translateY(-4px); }
.bc-cover { position: relative; width: 100%; aspect-ratio: 3/4; background: #f1f5f9; overflow: hidden; }
.bc-no-cover { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #cbd5e1; }
.bc-badge { position: absolute; top: 0; right: 0; padding: 5px 10px; font-size: 11px; font-weight: 700; color: #fff; border-radius: 0 0 0 8px; }
.bc-badge.sold { background: var(--danger); }
.bc-badge.low { background: var(--warning); }
.bc-info { padding: 10px 12px 12px; }
.bc-title { font-size: 14px; font-weight: 600; margin: 0 0 3px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.bc-author { font-size: 12px; color: var(--text-muted); margin: 0 0 10px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.bc-stock { font-size: 11px; color: var(--success); font-weight: 600; }
.bc-stock.low { color: var(--warning); }
.bc-stock.zero { color: var(--danger); }
.empty-wrap { grid-column: 1 / -1; padding: 60px 0; }
@media (max-width: 1400px) { .book-grid { grid-template-columns: repeat(5, 1fr); } }
@media (max-width: 1100px) { .book-grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 768px) { .book-grid { grid-template-columns: repeat(2, 1fr); gap: 10px; } }
</style>
