<template>
  <div class="role-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增角色
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="角色名称">
          <el-input v-model="queryForm.roleName" placeholder="请输入角色名称" clearable />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="queryForm.roleCode" placeholder="请输入角色编码" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 角色列表 -->
      <el-table :data="roleList" stripe style="width: 100%" v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="roleName" label="角色名称" min-width="120" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
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
            <el-button type="primary" link @click="handlePermission(row)">分配权限</el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码" :disabled="isEdit" />
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

    <!-- 分配权限对话框 -->
    <el-dialog v-model="permissionDialogVisible" title="分配权限" width="500px">
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        show-checkbox
        node-key="id"
        :props="{ label: 'menuName', children: 'children' }"
        default-expand-all
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPermission">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const roleList = ref([])
const menuTree = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const isEdit = ref(false)
const formRef = ref(null)
const menuTreeRef = ref(null)
const currentRole = ref(null)

const queryForm = reactive({
  roleName: '',
  roleCode: '',
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1
})

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

// 模拟数据
const mockRoles = [
  {
    id: 1,
    roleName: '超级管理员',
    roleCode: 'ROLE_ADMIN',
    description: '系统超级管理员，拥有所有权限',
    status: 1,
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 2,
    roleName: '医院管理员',
    roleCode: 'ROLE_HOSPITAL_ADMIN',
    description: '医院管理员，管理单个医院的数据',
    status: 1,
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 3,
    roleName: '普通用户',
    roleCode: 'ROLE_USER',
    description: '普通用户，只能查看数据',
    status: 1,
    createTime: '2024-01-01 10:00:00'
  }
]

const mockMenuTree = [
  {
    id: 1,
    menuName: '系统管理',
    children: [
      { id: 2, menuName: '用户管理' },
      { id: 3, menuName: '角色管理' },
      { id: 4, menuName: '菜单管理' }
    ]
  },
  {
    id: 5,
    menuName: '医院管理',
    children: [
      { id: 6, menuName: '医院列表' }
    ]
  },
  {
    id: 7,
    menuName: '监控管理',
    children: [
      { id: 8, menuName: '服务器监控' },
      { id: 9, menuName: 'MySQL监控' },
      { id: 10, menuName: 'Tomcat监控' },
      { id: 11, menuName: '告警管理' }
    ]
  },
  {
    id: 12,
    menuName: '数据大屏'
  }
]

// 获取角色列表
const fetchRoleList = async () => {
  loading.value = true
  try {
    // 模拟数据
    roleList.value = mockRoles
    total.value = mockRoles.length
    menuTree.value = mockMenuTree
  } catch (error) {
    console.error('获取角色列表失败', error)
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryForm.pageNum = 1
  fetchRoleList()
}

// 重置查询
const resetQuery = () => {
  queryForm.roleName = ''
  queryForm.roleCode = ''
  queryForm.pageNum = 1
  fetchRoleList()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增角色'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑角色'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 分配权限
const handlePermission = (row) => {
  currentRole.value = row
  permissionDialogVisible.value = true
  // 这里应该加载该角色已有的权限
}

// 提交权限分配
const submitPermission = async () => {
  const checkedKeys = menuTreeRef.value?.getCheckedKeys()
  const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys()
  const menuIds = [...checkedKeys, ...halfCheckedKeys]
  
  try {
    // await assignRoleMenus(currentRole.value.id, menuIds)
    ElMessage.success('权限分配成功')
    permissionDialogVisible.value = false
  } catch (error) {
    console.error('权限分配失败', error)
  }
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除角色 "${row.roleName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // await deleteRole(row.id)
      ElMessage.success('删除成功')
      fetchRoleList()
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
      // await updateRole(form)
      ElMessage.success('修改成功')
    } else {
      // await addRole(form)
      ElMessage.success('新增成功')
    }
    
    dialogVisible.value = false
    fetchRoleList()
  } catch (error) {
    console.error('提交失败', error)
  }
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.roleName = ''
  form.roleCode = ''
  form.description = ''
  form.status = 1
}

// 分页
const handleSizeChange = (val) => {
  queryForm.pageSize = val
  fetchRoleList()
}

const handleCurrentChange = (val) => {
  queryForm.pageNum = val
  fetchRoleList()
}

// 初始化
fetchRoleList()
</script>

<style scoped>
.role-manage {
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
