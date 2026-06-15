<template>
  <div class="admin-page">
    <div class="page-header"><h2>公告管理</h2><el-button type="primary" :icon="Plus" @click="openDlg(null)">新增公告</el-button></div>
    <el-card shadow="never" class="tbl-card">
      <el-table :data="list" stripe>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column label="置顶" width="80"><template #default="{row}"><el-tag :type="row.isTop?'danger':'info'" size="small">{{row.isTop?'置顶':'普通'}}</el-tag></template></el-table-column>
        <el-table-column label="状态" width="90"><template #default="{row}"><el-tag :type="row.status?'success':'info'" size="small">{{row.status?'显示':'隐藏'}}</el-tag></template></el-table-column>
        <el-table-column label="时间" width="170"><template #default="{row}">{{row.createTime?.replace('T',' ').substring(0,19)}}</template></el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{row}">
            <el-button type="primary" size="small" @click="openDlg(row)">编辑</el-button>
            <el-popconfirm title="删除？" @confirm="doDel(row.id)"><template #reference><el-button type="danger" size="small" plain>删除</el-button></template></el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dlg" :title="edit?.id?'编辑公告':'新增公告'" width="560px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="置顶"><el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" :loading="saving" @click="doSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAllAnnouncements, addAnnouncement, updateAnnouncement, delAnnouncement } from '@/api/announcement'

const list = ref([]), dlg = ref(false), saving = ref(false), edit = ref(null)
const form = reactive({ title:'', content:'', isTop:0, status:1 })

onMounted(() => fetch())
async function fetch() { try { const r = await getAllAnnouncements(); list.value = r.data } catch {} }

function openDlg(row) {
  edit.value = row
  if (row) { Object.assign(form, { title:row.title, content:row.content, isTop:row.isTop, status:row.status }) }
  else { Object.assign(form, { title:'', content:'', isTop:0, status:1 }) }
  dlg.value = true
}

async function doSave() {
  saving.value = true
  try {
    if (edit.value?.id) { await updateAnnouncement(edit.value.id, form) }
    else { await addAnnouncement(form) }
    ElMessage.success('保存成功'); dlg.value = false; fetch()
  } catch {} finally { saving.value = false }
}

async function doDel(id) { try { await delAnnouncement(id); ElMessage.success('已删除'); fetch() } catch {} }
</script>

<style scoped>
.admin-page { width: 100%; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; }
.tbl-card { border-radius: 12px; }
</style>
