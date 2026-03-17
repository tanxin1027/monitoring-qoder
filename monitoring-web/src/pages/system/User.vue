<template>
  <div class="user-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增用户
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="用户名">
          <el-input v-model="queryForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="queryForm.realName" placeholder="请输入真实姓名" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 用户列表 -->
      <el-table :data="userList" stripe style="width: 100%" v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="email" label="邮箱" min-width="150" />
        <el-table-column prop="phone" label="电话" width="120" />
        <el-table-column label="角色" min-width="150">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role" size="small" style="margin-right: 5px;">
              {{ role }}
            </el-tag>
          </template>
        </el-table-column>
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
            <el-button type="primary" link @click="handleResetPassword(row)">重置密码</el-button>
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
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%;">
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="医院权限" prop="hospitalIds">
          <el-select v-model="form.hospitalIds" multiple placeholder="请选择可查看的医院" style="width: 100%;">
            <el-option
              v-for="hospital in hospitalList"
              :key="hospital.id"
              :label="hospital.hospitalName"
              :value="hospital.id"
            />
          </el-select>
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
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const userList = ref([])
const roleList = ref([])
const hospitalList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const isEdit = ref(false)
const formRef = ref(null)

const queryForm = reactive({
  username: '',
  realName: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  id: null,
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  roleIds: [],
  hospitalIds: [],
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur', min: 6 }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

// 模拟数据
const mockUsers = [
  {
    id: 1,
    username: 'admin',
    realName: '系统管理员',
    email: 'admin@monitoring.com',
    phone: '13800138000',
    roles: ['超级管理员'],
    status: 1,
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 2,
    username: 'user1',
    realName: '张三',
    email: 'zhangsan@hospital.com',
    phone: '13800138001',
    roles: ['医院管理员'],
    status: 1,
    createTime: '2024-01-05 14:30:00'
  }
]

const mockRoles = [
  { id: 1, roleName: '超级管理员' },
  { id: 2, roleName: '医院管理员' },
  { id: 3, roleName: '普通用户' }
]

const mockHospitals = [
  { id: 1, hospitalName: '示例医院' },
  { id: 2, hospitalName: '第一人民医院' }
]

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    // 模拟数据
    userList.value = mockUsers
    total.value = mockUsers.length
    roleList.value = mockRoles
    hospitalList.value = mockHospitals
  } catch (error) {
    console.error('获取用户列表失败', error)
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryForm.pageNum = 1
  fetchUserList()
}

// 重置查询
const resetQuery = () => {
  queryForm.username = ''
  queryForm.realName = ''
  queryForm.status = ''
  queryForm.pageNum = 1
  fetchUserList()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 重置密码
const handleResetPassword = (row) => {
  ElMessageBox.prompt(`请输入用户 "${row.username}" 的新密码`, '重置密码', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^.{6,}$/,
    inputErrorMessage: '密码长度不能少于6位'
  }).then(({ value }) => {
    // await resetPassword(row.id, value)
    ElMessage.success('密码重置成功')
  })
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // await deleteUser(row.id)
      ElMessage.success('删除成功')
      fetchUserList()
    } catch (error) {
      console.error('删除失败', error)
    }
  })
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (isEdit.value) {
      // await updateUser(form)
      ElMessage.success('修改成功')
    } else {
      // await addUser(form)
      ElMessage.success('新增成功')
    }
    
    dialogVisible.value = false
    fetchUserList()
  } catch (error) {
    console.error('提交失败', error)
  }
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.username = ''
  form.password = ''
  form.realName = ''
  form.email = ''
  form.phone = ''
  form.roleIds = []
  form.hospitalIds = []
  form.status = 1
}

// 分页
const handleSizeChange = (val) => {
  queryForm.pageSize = val
  fetchUserList()
}

const handleCurrentChange = (val) => {
  queryForm.pageNum = val
  fetchUserList()
}

// 初始化
fetchUserList()
</script>

<style scoped>
.user-manage {
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
