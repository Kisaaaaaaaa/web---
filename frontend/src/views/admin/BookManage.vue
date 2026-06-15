<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>图书管理 ({{ total }}本)</h2>
      <el-button type="primary" :icon="Plus" @click="openDialog(null)">新增图书</el-button>
    </div>

    <div class="filter-bar">
      <el-input v-model="filter.keyword" placeholder="书名/作者/ISBN" clearable @keyup.enter="fetchBooks" style="width:200px" />
      <el-select v-model="filter.categoryId" placeholder="分类" clearable style="width:140px" @change="fetchBooks"><el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" /></el-select>
      <el-select v-model="filter.status" placeholder="状态" clearable style="width:100px" @change="fetchBooks"><el-option label="在架" :value="1"/><el-option label="下架" :value="0"/></el-select>
      <el-button type="primary" @click="fetchBooks">搜索</el-button>
    </div>

    <div class="table-card">
      <el-table :data="books" v-loading="loading" stripe @row-click="openDialog">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="isbn" label="ISBN" width="140" />
        <el-table-column prop="title" label="书名" min-width="180" />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="categoryName" label="分类" width="110" />
        <el-table-column label="库存" width="110"><template #default="{ row }">{{ row.currentStock }} / {{ row.totalStock }}</template></el-table-column>
        <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'在架':'下架' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button type="primary" size="small" @click.stop="openDialog(row)">编辑</el-button>
              <el-button :type="row.status===1?'warning':'success'" size="small" plain @click.stop="handleToggleStatus(row.id, row.status===1?0:1)">{{ row.status===1?'下架':'上架' }}</el-button>
              <el-button type="danger" size="small" plain @click.stop="handleDelete(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pg-wrap" v-if="total > query.pageSize">
      <el-pagination v-model:current-page="query.page" :page-size="query.pageSize" :total="total" layout="prev, pager, next, total" @current-change="fetchBooks" background />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingBook?.id ? '编辑图书' : '新增图书'" width="640px" destroy-on-close :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="ISBN" prop="isbn"><el-input v-model="form.isbn" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="书名" prop="title"><el-input v-model="form.title" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="作者" prop="author"><el-input v-model="form.author" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="出版社"><el-input v-model="form.publisher" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="分类" prop="categoryId"><el-select v-model="form.categoryId" style="width:100%"><el-option v-for="cat in categories" :key="cat.id" :label="cat.categoryName" :value="cat.id" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="总库存" prop="totalStock"><el-input-number v-model="form.totalStock" :min="0" :max="999" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="出版日期"><el-date-picker v-model="form.publishDate" type="date" style="width:100%" /></el-form-item>
        <el-form-item label="封面">
          <el-upload :auto-upload="false" :show-file-list="false" accept="image/*" @change="onCoverChange"><el-button size="small">选择本地图片</el-button></el-upload>
          <el-input v-model="form.coverUrl" style="margin-top:6px" />
          <el-image v-if="form.coverUrl" :src="form.coverUrl" fit="cover" style="width:80px;height:110px;border-radius:4px;margin-top:6px" />
        </el-form-item>
        <el-form-item label="简介"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="saving" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'; import { ElMessage, ElMessageBox } from 'element-plus'; import { Plus } from '@element-plus/icons-vue'
import { getBookList, addBook, updateBook, updateBookStatus, deleteBook } from '@/api/book'; import { getAllCategories } from '@/api/category'; import { uploadFile } from '@/api/review'

const loading = ref(false), books = ref([]), total = ref(0), categories = ref([])
const filter = reactive({ keyword: '', categoryId: null, status: null })
const query = reactive({ page: 1, pageSize: 10 })

const dialogVisible = ref(false), saving = ref(false), editingBook = ref(null), formRef = ref(null)
const form = reactive({ isbn:'',title:'',author:'',publisher:'',categoryId:null,totalStock:1,publishDate:null,coverUrl:'',description:'' })
const rules = { isbn:[{required:true}], title:[{required:true}], author:[{required:true}], categoryId:[{required:true}], totalStock:[{required:true}] }

onMounted(async () => { await loadCategories(); fetchBooks() })
async function loadCategories() { try { categories.value = (await getAllCategories()).data } catch {} }
async function fetchBooks() {
  loading.value = true
  try {
    const params = { page: query.page, pageSize: query.pageSize }
    if (filter.keyword) params.keyword = filter.keyword
    if (filter.categoryId) params.categoryId = filter.categoryId
    if (filter.status != null) params.status = filter.status
    const r = await getBookList(params)
    books.value = r.data.list
    total.value = r.data.total
  } catch {} finally { loading.value = false }
}

function openDialog(book) { editingBook.value = book; if (book) Object.assign(form, { isbn:book.isbn,title:book.title,author:book.author,publisher:book.publisher,categoryId:book.categoryId,totalStock:book.totalStock,publishDate:book.publishDate,coverUrl:book.coverUrl,description:book.description }); else Object.assign(form, { isbn:'',title:'',author:'',publisher:'',categoryId:null,totalStock:1,publishDate:null,coverUrl:'',description:'' }); dialogVisible.value = true }
async function handleSave() { if (!await formRef.value.validate().catch(()=>false)) return; saving.value = true; try { if (editingBook.value?.id) { await updateBook(editingBook.value.id, form) } else { await addBook(form) }; ElMessage.success('保存成功'); dialogVisible.value = false; fetchBooks() } catch {} finally { saving.value = false } }
async function onCoverChange(file) { try { form.coverUrl = (await uploadFile(file.raw)).data.url } catch {} }
async function handleToggleStatus(id, status) { try { await updateBookStatus(id, status); ElMessage.success(status?'已上架':'已下架'); fetchBooks() } catch {} }
async function handleDelete(id) { try { await ElMessageBox.confirm('确认删除该图书？', '删除', { type: 'warning' }); await deleteBook(id); ElMessage.success('已删除'); fetchBooks() } catch {} }
</script>

<style scoped>
.admin-page { width: 100%; }
.filter-bar { display: flex; gap: 10px; flex-wrap: wrap; align-items: center; margin-bottom: 16px; background: var(--surface); padding: 14px 18px; border-radius: var(--radius); box-shadow: var(--shadow); }
.table-card { background: var(--surface); border-radius: var(--radius); box-shadow: var(--shadow); padding: 20px 24px; }
.pg-wrap { display: flex; justify-content: center; margin-top: 20px; }
.action-btns { display: flex; gap: 6px; }
</style>
