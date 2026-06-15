<template>
  <div class="book-detail-card">
    <!-- ==================== 卡片主体：左右分栏 ==================== -->
    <div class="card-body">
      <!-- 左侧：图书封面 -->
      <div class="card-left">
        <div class="cover-wrapper">
          <el-image
            :src="book.coverUrl"
            :alt="book.title"
            :preview-src-list="[book.coverUrl]"
            :preview-teleported="true"
            fit="cover"
            lazy
            class="book-cover"
          >
            <!-- 封面加载失败时的占位图 -->
            <template #error>
              <div class="cover-placeholder">
                <el-icon :size="48"><PictureFilled /></el-icon>
                <span>暂无封面</span>
              </div>
            </template>
            <!-- 封面加载中的占位图 -->
            <template #placeholder>
              <div class="cover-placeholder">
                <el-icon :size="48" class="is-loading"><Loading /></el-icon>
                <span>加载中...</span>
              </div>
            </template>
          </el-image>
          <!-- 图书状态标签 -->
          <div class="cover-badge" :class="stockBadgeClass">
            {{ stockBadgeText }}
          </div>
        </div>
      </div>

      <!-- 右侧：详细信息 -->
      <div class="card-right">
        <!-- 书名与作者 -->
        <div class="book-header">
          <h2 class="book-title">{{ book.title }}</h2>
          <p class="book-author">
            <el-icon><User /></el-icon>
            {{ book.author }}
          </p>
        </div>

        <!-- 结构化信息 -->
        <el-descriptions
          :column="2"
          border
          size="default"
          class="book-descriptions"
        >
          <el-descriptions-item label="ISBN" :span="2">
            <code class="isbn-code">{{ book.isbn }}</code>
          </el-descriptions-item>
          <el-descriptions-item label="出版社">
            {{ book.publisher || '暂无信息' }}
          </el-descriptions-item>
          <el-descriptions-item label="出版日期">
            {{ book.publishDate || '暂无信息' }}
          </el-descriptions-item>
          <el-descriptions-item label="分类">
            <el-tag size="small" effect="plain" round>
              {{ book.categoryName || '未分类' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="语言">
            {{ book.language || '中文' }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 库存进度条 -->
        <div class="stock-section">
          <div class="stock-header">
            <span class="stock-label">
              <el-icon><Box /></el-icon>
              馆藏库存
            </span>
            <span class="stock-text" :class="{ 'stock-low': book.currentStock <= 2 && book.currentStock > 0 }">
              {{ book.currentStock }} / {{ book.totalStock }} 本可借
            </span>
          </div>
          <el-progress
            :percentage="stockPercentage"
            :color="progressColor"
            :stroke-width="14"
            :striped="book.currentStock <= 2 && book.currentStock > 0"
            :striped-flow="book.currentStock <= 2 && book.currentStock > 0"
            :duration="3"
          >
            <template #default="{ percentage }">
              <span class="progress-inner-text">{{ percentage }}%</span>
            </template>
          </el-progress>
          <!-- 库存预警提示 -->
          <transition name="fade-bounce">
            <p v-if="book.currentStock === 0" class="stock-warning sold-out">
              <el-icon><WarningFilled /></el-icon>
              该图书已全部借出，暂时无法借阅
            </p>
            <p v-else-if="book.currentStock <= 2" class="stock-warning low-stock">
              <el-icon><Warning /></el-icon>
              库存紧张，仅剩 {{ book.currentStock }} 本，请尽快借阅
            </p>
          </transition>
        </div>

        <!-- 图书简介 -->
        <div class="book-desc" v-if="book.description">
          <p class="desc-label">
            <el-icon><Reading /></el-icon>
            内容简介
          </p>
          <p class="desc-content">{{ book.description }}</p>
        </div>

        <!-- 操作按钮区 -->
        <div class="card-actions">
          <el-button
            type="primary"
            size="large"
            :disabled="book.currentStock <= 0"
            :loading="borrowing"
            @click="handleBorrow"
            class="borrow-btn"
            round
          >
            <el-icon v-if="!borrowing"><Promotion /></el-icon>
            {{ borrowButtonText }}
          </el-button>
          <el-button
            size="large"
            @click="handleFavorite"
            class="favorite-btn"
            :class="{ 'is-favorited': isFavorited }"
            round
            :plain="!isFavorited"
            :type="isFavorited ? 'warning' : 'default'"
          >
            <el-icon><StarFilled v-if="isFavorited" /><Star v-else /></el-icon>
            {{ isFavorited ? '已收藏' : '收藏' }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  PictureFilled, Loading, User, Box, Warning, WarningFilled,
  Reading, Promotion, Star, StarFilled
} from '@element-plus/icons-vue'
import { toggleFavorite, checkFavorite } from '@/api/favorite'

// ==================== Props ====================

const props = defineProps({
  /** 图书信息对象 */
  book: {
    type: Object,
    required: true,
    default: () => ({
      id: null,
      isbn: '',
      title: '未知图书',
      author: '未知作者',
      publisher: '',
      publishDate: '',
      coverUrl: '',
      categoryName: '',
      description: '',
      totalStock: 0,
      currentStock: 0
    })
  }
})

// ==================== Emits ====================

const emit = defineEmits(['borrow', 'favorite'])

// ==================== 状态 ====================

const borrowing = ref(false)

// ==================== 计算属性 ====================

/** 库存百分比 */
const stockPercentage = computed(() => {
  if (!props.book.totalStock || props.book.totalStock === 0) return 0
  return Math.round((props.book.currentStock / props.book.totalStock) * 100)
})

/** 进度条颜色（根据库存动态变化） */
const progressColor = computed(() => {
  if (props.book.currentStock === 0) return '#F56C6C'       // 红色：无库存
  if (props.book.currentStock <= 2) return '#E6A23C'         // 橙色：库存紧张
  if (props.book.currentStock <= 5) return '#409EFF'         // 蓝色：库存一般
  return '#67C23A'                                            // 绿色：库存充足
})

/** 封面上角标样式 */
const stockBadgeClass = computed(() => ({
  'badge-available': props.book.currentStock > 2,
  'badge-low': props.book.currentStock > 0 && props.book.currentStock <= 2,
  'badge-sold-out': props.book.currentStock === 0
}))

/** 封面上角标文字 */
const stockBadgeText = computed(() => {
  if (props.book.currentStock === 0) return '已借完'
  if (props.book.currentStock <= 2) return '库存紧张'
  return '可借阅'
})

/** 借阅按钮文字 */
const borrowButtonText = computed(() => {
  if (props.book.currentStock === 0) return '暂无库存'
  if (props.book.currentStock <= 2) return `仅剩 ${props.book.currentStock} 本 · 立即借阅`
  return '立即自助借阅'
})

// ==================== 方法 ====================

/** 借阅操作 */
const handleBorrow = async () => {
  if (props.book.currentStock <= 0) {
    ElMessage.warning('该图书暂无库存，无法借阅')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认借阅《${props.book.title}》吗？借阅期限为 30 天，请按时归还。`,
      '确认借阅',
      {
        confirmButtonText: '确认借阅',
        cancelButtonText: '我再想想',
        type: 'info',
        roundButton: true
      }
    )
    borrowing.value = true
    emit('borrow', props.book)
  } catch {
    // 用户取消
  } finally {
    setTimeout(() => {
      borrowing.value = false
    }, 600)
  }
}

// ==================== 收藏状态 ====================

const isFavorited = ref(false)

watch(() => props.book?.id, async (newId) => {
  if (!newId) return
  isFavorited.value = false
  try {
    const res = await checkFavorite(newId)
    isFavorited.value = res.data
  } catch {}
}, { immediate: true })

/** 收藏/取消收藏 */
const handleFavorite = async () => {
  if (!props.book?.id) return
  try {
    const res = await toggleFavorite(props.book.id)
    isFavorited.value = res.data
    ElMessage.success(isFavorited.value ? `已收藏《${props.book.title}》` : `已取消收藏《${props.book.title}》`)
    emit('favorite', props.book)
  } catch {}
}
</script>

<style scoped>
/* ============================================================
   基础变量
   ============================================================ */
.book-detail-card {
  --card-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  --card-shadow-hover: 0 12px 40px rgba(0, 0, 0, 0.12);
  --card-radius: 16px;
  --primary: #409eff;
  --primary-dark: #337ecc;
  --primary-light: #ecf5ff;
  --text-primary: #303133;
  --text-secondary: #606266;
  --text-muted: #909399;

  max-width: 960px;
  margin: 0 auto;
  background: #ffffff;
  border-radius: var(--card-radius);
  box-shadow: var(--card-shadow);
  overflow: hidden;
  transition: box-shadow 0.3s ease, transform 0.3s ease;
}

.book-detail-card:hover {
  box-shadow: var(--card-shadow-hover);
  transform: translateY(-2px);
}

/* ============================================================
   左右分栏布局
   ============================================================ */
.card-body {
  display: flex;
  gap: 0;
}

/* ---------- 左侧封面 ---------- */
.card-left {
  flex: 0 0 300px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.card-left::before {
  content: '';
  position: absolute;
  top: -60px;
  right: -60px;
  width: 180px;
  height: 180px;
  background: var(--primary-light);
  border-radius: 50%;
  opacity: 0.4;
}

.cover-wrapper {
  position: relative;
  z-index: 1;
  width: 200px;
}

.book-cover {
  width: 200px;
  height: 280px;
  border-radius: 8px;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.15);
  transition: transform 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  cursor: pointer;
}

.book-cover:hover {
  transform: scale(1.04);
}

/* 封面占位 */
.cover-placeholder {
  width: 200px;
  height: 280px;
  border-radius: 8px;
  background: #e8ecf1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--text-muted);
  font-size: 13px;
}

/* 封面角标 */
.cover-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  z-index: 2;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 1px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.badge-available {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.badge-low {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
  animation: badge-pulse 2s ease-in-out infinite;
}

.badge-sold-out {
  background: linear-gradient(135deg, #f56c6c, #f89898);
}

@keyframes badge-pulse {
  0%, 100% { box-shadow: 0 2px 8px rgba(230, 162, 60, 0.3); }
  50%      { box-shadow: 0 4px 16px rgba(230, 162, 60, 0.6); }
}

/* ---------- 右侧详情 ---------- */
.card-right {
  flex: 1;
  padding: 28px 32px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-width: 0;
}

/* 图书标题区 */
.book-header {
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f2f5;
}

.book-title {
  margin: 0 0 8px 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.3;
  letter-spacing: 0.5px;
}

.book-author {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 结构化描述 */
.book-descriptions :deep(.el-descriptions__label) {
  font-weight: 600;
  color: var(--text-secondary);
  width: 90px;
}

.book-descriptions :deep(.el-descriptions__content) {
  color: var(--text-primary);
}

.isbn-code {
  font-family: 'SF Mono', 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  color: var(--primary);
  background: var(--primary-light);
  padding: 2px 10px;
  border-radius: 4px;
  letter-spacing: 1px;
}

/* ---------- 库存进度条区 ---------- */
.stock-section {
  background: #fafbfc;
  border-radius: 10px;
  padding: 16px 18px;
  border: 1px solid #ebeef5;
}

.stock-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.stock-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 6px;
}

.stock-text {
  font-size: 14px;
  font-weight: 600;
  color: #67c23a;
  transition: color 0.3s;
}

.stock-text.stock-low {
  color: #e6a23c;
}

.progress-inner-text {
  font-size: 11px;
  font-weight: 600;
}

/* 库存预警提示 */
.stock-warning {
  margin: 10px 0 0 0;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 6px;
}

.stock-warning.low-stock {
  color: #e6a23c;
  background: #fdf6ec;
  border: 1px solid #faecd8;
}

.stock-warning.sold-out {
  color: #f56c6c;
  background: #fef0f0;
  border: 1px solid #fde2e2;
}

/* ---------- 图书简介 ---------- */
.book-desc {
  background: #f8f9fb;
  border-radius: 8px;
  padding: 14px 16px;
  border-left: 3px solid var(--primary);
}

.desc-label {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 6px;
}

.desc-content {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* ---------- 操作按钮区 ---------- */
.card-actions {
  display: flex;
  gap: 14px;
  margin-top: auto;
  padding-top: 6px;
}

.borrow-btn {
  flex: 1;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  background: linear-gradient(135deg, #409eff, #337ecc);
  border: none;
  transition: all 0.3s ease;
}

.borrow-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #66b1ff, #409eff);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.35);
  transform: translateY(-1px);
}

.borrow-btn:active:not(:disabled) {
  transform: translateY(0);
}

.borrow-btn:disabled {
  background: #c0c4cc;
  cursor: not-allowed;
}

.favorite-btn {
  width: 120px;
  height: 48px;
  font-size: 14px;
  font-weight: 500;
  border-color: #dcdfe6;
  transition: all 0.3s ease;
}

.favorite-btn:hover {
  border-color: #e6a23c;
  color: #e6a23c;
}
.favorite-btn.is-favorited {
  background: #fdf6ec;
  border-color: #e6a23c;
  color: #e6a23c;
}

/* ============================================================
   过渡动画
   ============================================================ */
.fade-bounce-enter-active {
  animation: fade-in-bounce 0.4s ease;
}

.fade-bounce-leave-active {
  animation: fade-in-bounce 0.25s ease reverse;
}

@keyframes fade-in-bounce {
  0%   { opacity: 0; transform: translateY(8px); }
  60%  { opacity: 1; transform: translateY(-2px); }
  100% { opacity: 1; transform: translateY(0); }
}

/* ============================================================
   响应式：窄屏（≤768px）切换为上下布局
   ============================================================ */
@media screen and (max-width: 768px) {
  .card-body {
    flex-direction: column;
  }

  .card-left {
    flex: 0 0 auto;
    padding: 24px 16px;
  }

  .book-cover,
  .cover-placeholder {
    width: 160px;
    height: 224px;
  }

  .card-right {
    padding: 20px;
  }

  .book-title {
    font-size: 18px;
  }

  .card-actions {
    flex-direction: column;
  }

  .favorite-btn {
    width: 100%;
  }
}
</style>
