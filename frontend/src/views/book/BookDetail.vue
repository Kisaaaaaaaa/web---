<template>
  <div class="book-detail-page" v-loading="loading">
    <div v-if="book">
      <!-- 返回按钮 -->
      <el-button text :icon="ArrowLeft" @click="$router.back()" class="back-btn">
        返回列表
      </el-button>

      <!-- 图书详情卡片 -->
      <BookDetailCard :book="book" @borrow="handleBorrow" />
    </div>

    <!-- 评论区 -->
    <div class="review-section" v-if="book">
      <el-card shadow="never" class="review-card">
        <template #header>
          <div class="review-header">
            <span><el-icon style="vertical-align:-2px"><ChatDotSquare /></el-icon> 读者评论（{{ reviewStats.count }}条，均分 {{ reviewStats.avgRating }}）</span>
          </div>
        </template>
        <!-- 写评论 -->
        <div class="write-review">
          <el-input v-model="newReview.content" type="textarea" :rows="2" placeholder="写下你的评论..." />
          <div class="review-actions">
            <el-rate v-model="newReview.rating" :max="5" show-score />
            <el-button type="primary" :loading="reviewSending" @click="submitReview">发表评论</el-button>
          </div>
        </div>
        <el-divider />
        <!-- 评论列表 -->
        <div class="review-list" v-loading="reviewsLoading">
          <div v-for="r in reviews" :key="r.id" class="review-item">
            <div class="ri-header">
              <el-avatar :size="32">{{ r.realName?.charAt(0) }}</el-avatar>
              <div>
                <span class="ri-user">{{ r.realName }}</span>
                <el-rate :model-value="r.rating" :max="5" disabled size="small" style="display:inline-flex;margin-left:8px" />
              </div>
              <span class="ri-time">{{ fmt(r.createTime) }}</span>
            </div>
            <p class="ri-content">{{ r.content }}</p>
          </div>
          <el-empty v-if="!reviewsLoading && reviews.length===0" description="暂无评论，来写第一条吧" :image-size="60" />
        </div>
      </el-card>
    </div>

    <el-empty v-else-if="!loading" description="图书不存在" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ChatDotSquare } from '@element-plus/icons-vue'
import { getBookDetail } from '@/api/book'
import { borrowBook } from '@/api/borrow'
import { getReviews, getReviewStats, addReview } from '@/api/review'
import BookDetailCard from '@/components/book/BookDetailCard.vue'

const route = useRoute()
const book = ref(null)
const loading = ref(false)

// 评论
const reviews = ref([])
const reviewStats = reactive({ count: 0, avgRating: 0 })
const reviewsLoading = ref(false)
const reviewSending = ref(false)
const newReview = reactive({ content: '', rating: 5 })

onMounted(async () => {
  const id = route.params.id
  loading.value = true
  try { const res = await getBookDetail(id); book.value = res.data } catch { book.value = null } finally { loading.value = false }
  loadReviews()
})

async function loadReviews() {
  const id = route.params.id
  reviewsLoading.value = true
  try { const r = await getReviews(id); reviews.value = r.data } catch {} finally { reviewsLoading.value = false }
  try { const r = await getReviewStats(id); Object.assign(reviewStats, r.data) } catch {}
}

async function submitReview() {
  if (!newReview.content.trim()) { ElMessage.warning('请输入评论'); return }
  reviewSending.value = true
  try {
    await addReview({ bookId: Number(route.params.id), content: newReview.content, rating: newReview.rating })
    ElMessage.success('评论成功')
    newReview.content = ''; newReview.rating = 5
    loadReviews()
  } catch {} finally { reviewSending.value = false }
}

function fmt(t) { return t?.replace('T',' ').substring(0,19) || '-' }

async function handleBorrow(bookData) {
  try {
    await borrowBook(bookData.id)
    ElMessage.success('借阅成功！请于30天内归还')
    const res = await getBookDetail(bookData.id)
    book.value = res.data
  } catch {}
}
</script>

<style scoped>
.book-detail-page {
  max-width: 1000px;
  margin: 0 auto;
}
.back-btn { margin-bottom: 16px; font-size: 14px; }

/* 评论区 */
.review-section { margin-top: 24px; }
.review-card { border-radius: 12px; }
.review-header { font-size: 16px; font-weight: 600; }
.write-review { margin-bottom: 8px; }
.review-actions { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; }
.review-list { min-height: 100px; }
.review-item { padding: 16px 0; border-bottom: 1px solid #f0f2f5; }
.review-item:last-child { border-bottom: none; }
.ri-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.ri-user { font-weight: 600; font-size: 14px; }
.ri-time { margin-left: auto; font-size: 12px; color: #c0c4cc; }
.ri-content { margin: 0; font-size: 14px; color: #606266; line-height: 1.6; }
</style>
