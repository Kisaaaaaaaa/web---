<template>
  <div class="category-page">
    <div class="page-header">
      <div class="header-left">
        <h2>分类管理</h2>
        <el-tag type="info" size="small" effect="plain" round>
          共 {{ categories.length }} 个分类
        </el-tag>
      </div>
      <el-button type="primary" :icon="Plus" size="large" @click="openDialog(null)">
        新增分类
      </el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table
        :data="categories"
        v-loading="loading"
        stripe
        row-key="id"
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        class="category-table"
        empty-text="暂无分类数据，请点击「新增分类」添加"
      >
        <!-- 分类名称：带层级缩进和图标 -->
        <el-table-column label="分类名称" min-width="280" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="category-name-cell" :class="{ 'is-child': row.parentId !== 0 }">
              <el-icon :size="18" :color="row.parentId === 0 ? '#409eff' : '#909399'">
                <FolderOpened v-if="row.parentId === 0" />
                <Document v-else />
              </el-icon>
              <span class="cat-name">{{ row.categoryName }}</span>
              <el-tag
                v-if="row.parentId === 0"
                type="primary"
                size="small"
                effect="plain"
                class="parent-badge"
              >父级</el-tag>
            </div>
          </template>
        </el-table-column>

        <!-- 父级分类：显示名称而非ID -->
        <el-table-column label="父级分类" width="160" align="center">
          <template #default="{ row }">
            <span v-if="row.parentId === 0" class="text-muted">—</span>
            <el-tag v-else size="small" effect="plain" type="info" round>
              {{ getParentName(row.parentId) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 排序 -->
        <el-table-column prop="sortOrder" label="排序" width="100" align="center" sortable>
          <template #default="{ row }">
            <span class="sort-num">{{ row.sortOrder }}</span>
          </template>
        </el-table-column>

        <!-- 创建时间 -->
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            <span class="time-text">{{ fmtTime(row.createTime) }}</span>
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button type="primary" size="default" :icon="Edit" @click="openDialog(row)">
                编辑
              </el-button>
              <el-divider direction="vertical" />
              <el-popconfirm
                :title="'确定删除「' + row.categoryName + '」吗？若有子分类则无法删除。'"
                confirm-button-text="确认删除"
                cancel-button-text="取消"
                @confirm="handleDelete(row.id)"
              >
                <template #reference>
                  <el-button type="danger" link size="default" :icon="Delete">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingCat?.id ? '编辑分类' : '新增分类'"
      width="560px"
      destroy-on-close
      :close-on-click-modal="false"
      top="10vh"
    >
      <div class="dialog-body">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          label-position="right"
          size="large"
        >
          <el-form-item label="分类名称" prop="categoryName">
            <el-input
              v-model="form.categoryName"
              placeholder="请输入分类名称，如「计算机科学」"
              :prefix-icon="Edit"
              maxlength="50"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="父级分类" prop="parentId">
            <el-tree-select
              v-model="form.parentId"
              :data="treeSelectData"
              :render-after-expand="false"
              placeholder="不选则为顶级分类"
              check-strictly
              clearable
              :props="{ label: 'categoryName', value: 'id' }"
              style="width: 100%"
            >
              <template #default="{ data }">
                <span>{{ data.categoryName }}</span>
                <el-tag v-if="data.parentId === 0" size="small" effect="plain" style="margin-left:8px">
                  父级
                </el-tag>
              </template>
            </el-tree-select>
          </el-form-item>

          <el-form-item label="排序序号" prop="sortOrder">
            <el-input-number
              v-model="form.sortOrder"
              :min="0"
              :max="999"
              controls-position="right"
              style="width: 100%"
            />
            <span class="form-hint">数值越小越靠前，建议设置为 0、1、2...</span>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button size="large" @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" size="large" :loading="saving" @click="handleSave">
            {{ editingCat?.id ? '保存修改' : '确认创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, FolderOpened, Document, Edit, Delete } from '@element-plus/icons-vue'
import { getAllCategories, addCategory, updateCategory, deleteCategory } from '@/api/category'

const loading = ref(false)
const categories = ref([])
const parentOptions = ref([])

// el-tree-select 使用的数据（顶级 + "顶级分类"虚拟节点）
const treeSelectData = computed(() => {
  return categories.value.map(c => ({
    id: c.id,
    categoryName: c.categoryName,
    parentId: c.parentId
  }))
})

const dialogVisible = ref(false)
const saving = ref(false)
const editingCat = ref(null)
const formRef = ref(null)
const form = reactive({ categoryName: '', parentId: null, sortOrder: 0 })

const rules = {
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2-50 之间', trigger: 'blur' }
  ]
}

onMounted(() => fetchCategories())

async function fetchCategories() {
  loading.value = true
  try {
    const res = await getAllCategories()
    categories.value = res.data
    parentOptions.value = res.data
  } catch {} finally { loading.value = false }
}

function getParentName(parentId) {
  const parent = parentOptions.value.find(c => c.id === parentId)
  return parent ? parent.categoryName : '未知'
}

function openDialog(cat) {
  editingCat.value = cat
  if (cat) {
    form.categoryName = cat.categoryName
    form.parentId = cat.parentId === 0 ? null : cat.parentId
    form.sortOrder = cat.sortOrder
  } else {
    form.categoryName = ''
    form.parentId = null
    form.sortOrder = 0
  }
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  const payload = { ...form, parentId: form.parentId || 0 }
  try {
    if (editingCat.value?.id) {
      await updateCategory(editingCat.value.id, payload)
      ElMessage.success('分类已更新')
    } else {
      await addCategory(payload)
      ElMessage.success('分类已创建')
    }
    dialogVisible.value = false
    fetchCategories()
  } catch {} finally { saving.value = false }
}

async function handleDelete(id) {
  try {
    await deleteCategory(id)
    ElMessage.success('分类已删除')
    fetchCategories()
  } catch {}
}

function fmtTime(t) {
  return t ? t.replace('T', ' ').substring(0, 19) : '-'
}
</script>

<style scoped>
/* ============================================================
   页面布局
   ============================================================ */
.category-page {
  /* 去掉原来的 max-width 限制，撑满可用空间 */
  width: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left h2 {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  margin: 0;
}

/* ============================================================
   表格卡片
   ============================================================ */
.table-card {
  border-radius: 12px;
}

.table-card :deep(.el-card__body) {
  padding: 20px 24px;
}

.category-table {
  font-size: 14px;
}

/* 表格行高度 */
.category-table :deep(.el-table__row) {
  min-height: 52px;
}

.category-table :deep(.el-table__body tr) {
  transition: background-color 0.2s ease;
}

/* ============================================================
   分类名称列
   ============================================================ */
.category-name-cell {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0;
}

.cat-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.parent-badge {
  margin-left: 4px;
  font-size: 11px;
}

/* 子分类微缩进 */
.category-name-cell.is-child {
  padding-left: 8px;
}

/* ============================================================
   排序数字
   ============================================================ */
.sort-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f0f2f5;
  font-weight: 700;
  color: #606266;
  font-size: 14px;
}

/* ============================================================
   时间文本
   ============================================================ */
.time-text {
  font-size: 13px;
  color: #909399;
}

/* ============================================================
   操作按钮
   ============================================================ */
.action-btns {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.action-btns .el-divider--vertical {
  height: 1em;
}

/* ============================================================
   对话框
   ============================================================ */
.dialog-body {
  padding: 8px 0;
}

.form-hint {
  display: block;
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 6px;
  line-height: 1;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* ============================================================
   通用辅助
   ============================================================ */
.text-muted {
  color: #c0c4cc;
  font-size: 14px;
}

/* 表格表头 */
.category-table :deep(th.el-table__cell) {
  background: #f5f7fa;
  color: #303133;
  font-weight: 600;
  font-size: 14px;
}

/* 空状态 */
.category-table :deep(.el-table__empty-block) {
  min-height: 160px;
}
</style>
