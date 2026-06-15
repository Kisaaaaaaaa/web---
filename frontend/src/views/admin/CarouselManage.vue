<template>
  <div class="admin-page">
    <div class="page-header"><h2>轮播图管理</h2><el-button type="primary" :icon="Plus" @click="openDlg(null)">新增</el-button></div>
    <el-card shadow="never" class="tbl-card">
      <el-table :data="list" stripe>
        <el-table-column label="预览" width="200">
          <template #default="{row}"><el-image :src="row.imageUrl" fit="cover" style="width:180px;height:70px;border-radius:6px" /></template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{row}"><el-tag :type="row.status?'success':'info'" size="small">{{row.status?'启用':'禁用'}}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{row}">
            <el-button type="primary" size="small" @click="openDlg(row)">编辑</el-button>
            <el-popconfirm title="删除？" @confirm="doDel(row.id)"><template #reference><el-button type="danger" size="small" plain>删除</el-button></template></el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dlg" :title="edit?.id?'编辑轮播图':'新增轮播图'" width="500px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="跳转链接"><el-input v-model="form.linkUrl" placeholder="如 /books 留空则不跳转" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="图片上传">
          <el-upload :auto-upload="false" :show-file-list="false" accept="image/*" @change="onFileChange">
            <el-button>选择图片</el-button>
          </el-upload>
          <el-image v-if="previewUrl" :src="previewUrl" fit="cover" style="width:200px;height:80px;border-radius:6px;margin-top:8px" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" :loading="saving" @click="doSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAllCarousels, addCarousel, updateCarousel, delCarousel } from '@/api/carousel'
import { uploadFile } from '@/api/review'

const list = ref([]), dlg = ref(false), saving = ref(false), edit = ref(null), previewUrl = ref('')
const form = reactive({ title:'', linkUrl:'', sortOrder:0, status:1, imageUrl:'' })

onMounted(() => fetch())

async function fetch() { try { const r = await getAllCarousels(); list.value = r.data } catch {} }

function openDlg(row) {
  edit.value = row; previewUrl.value = row?.imageUrl || ''
  if (row) { Object.assign(form, { title:row.title, linkUrl:row.linkUrl||'', sortOrder:row.sortOrder, status:row.status, imageUrl:row.imageUrl }) }
  else { Object.assign(form, { title:'', linkUrl:'', sortOrder:0, status:1, imageUrl:'' }); previewUrl.value = '' }
  dlg.value = true
}

async function onFileChange(file) {
  try {
    const res = await uploadFile(file.raw)
    form.imageUrl = res.data.url
    previewUrl.value = res.data.url
    ElMessage.success('上传成功')
  } catch {}
}

async function doSave() {
  saving.value = true
  try {
    if (edit.value?.id) { await updateCarousel(edit.value.id, form) }
    else { await addCarousel(form) }
    ElMessage.success('保存成功'); dlg.value = false; fetch()
  } catch {} finally { saving.value = false }
}

async function doDel(id) { try { await delCarousel(id); ElMessage.success('已删除'); fetch() } catch {} }
</script>

<style scoped>
.admin-page { width: 100%; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; }
.tbl-card { border-radius: 12px; }
</style>
