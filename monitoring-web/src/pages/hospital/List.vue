<template>
  <div class="hospital-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>医院管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增医院
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="医院名称">
          <el-input v-model="queryForm.hospitalName" placeholder="请输入医院名称" clearable />
        </el-form-item>
        <el-form-item label="医院编码">
          <el-input v-model="queryForm.hospitalCode" placeholder="请输入医院编码" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 医院列表 -->
      <el-table :data="hospitalList" stripe style="width: 100%" v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="hospitalName" label="医院名称" min-width="150" />
        <el-table-column prop="hospitalCode" label="医院编码" width="120" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleViewServers(row)">查看服务器</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="医院名称" prop="hospitalName">
          <el-input v-model="form.hospitalName" placeholder="请输入医院名称" />
        </el-form-item>
        <el-form-item label="医院编码" prop="hospitalCode">
          <el-input v-model="form.hospitalCode" placeholder="请输入医院编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const hospitalList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增医院')
const isEdit = ref(false)
const formRef = ref(null)

const queryForm = reactive({
  hospitalName: '',
  hospitalCode: '',
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  id: null,
  hospitalName: '',
  hospitalCode: '',
  address: '',
  contactPerson: '',
  contactPhone: '',
  description: '',
  status: 1
})

const rules = {
  hospitalName: [{ required: true, message: '请输入医院名称', trigger: 'blur' }],
  hospitalCode: [{ required: true, message: '请输入医院编码', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }]
}

// 模拟数据
const mockData = [
  {
    id: 1,
    hospitalName: '示例医院',
    hospitalCode: 'DEMO001',
    address: '北京市朝阳区示例路1号',
    contactPerson: '张医生',
    contactPhone: '13800138001',
    description: '示例医院，用于测试',
    status: 1,
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 2,
    hospitalName: '第一人民医院',
    hospitalCode: 'HOSP001',
    address: '上海市黄浦区人民路100号',
    contactPerson: '李主任',
    contactPhone: '13800138002',
    description: '市级三甲医院',
    status: 1,
    createTime: '2024-01-05 14:30:00'
  }
]

// 获取医院列表
const fetchHospitalList = async () => {
  loading.value = true
  try {
    // 这里应该调用实际的API
    // const res = await getHospitalList(queryForm)
    // hospitalList.value = res.data.list || []
    // total.value = res.data.total || 0
    
    // 模拟数据
    hospitalList.value = mockData
    total.value = mockData.length
  } catch (error) {
    console.error('获取医院列表失败', error)
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryForm.pageNum = 1
  fetchHospitalList()
}

// 重置查询
const resetQuery = () => {
  queryForm.hospitalName = ''
  queryForm.hospitalCode = ''
  queryForm.pageNum = 1
  fetchHospitalList()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增医院'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑医院'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除医院 "${row.hospitalName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // await deleteHospital(row.id)
      ElMessage.success('删除成功')
      fetchHospitalList()
    } catch (error) {
      console.error('删除失败', error)
    }
  })
}

// 查看服务器
const handleViewServers = (row) => {
  router.push({
    path: '/monitor/server',
    query: { hospitalId: row.id }
  })
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (isEdit.value) {
      // await updateHospital(form)
      ElMessage.success('修改成功')
    } else {
      // await addHospital(form)
      ElMessage.success('新增成功')
    }
    
    dialogVisible.value = false
    fetchHospitalList()
  } catch (error) {
    console.error('提交失败', error)
  }
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.hospitalName = ''
  form.hospitalCode = ''
  form.address = ''
  form.contactPerson = ''
  form.contactPhone = ''
  form.description = ''
  form.status = 1
}

// 分页
const handleSizeChange = (val) => {
  queryForm.pageSize = val
  fetchHospitalList()
}

const handleCurrentChange = (val) => {
  queryForm.pageNum = val
  fetchHospitalList()
}

// 初始化
fetchHospitalList()
</script>

<style scoped>
.hospital-list {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.query-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
