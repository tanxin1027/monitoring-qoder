<template>
  <div class="menu-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>菜单管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增菜单
          </el-button>
        </div>
      </template>

      <!-- 菜单表格 -->
      <el-table
        :data="menuList"
        stripe
        style="width: 100%"
        row-key="id"
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        v-loading="loading"
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="150">
          <template #default="{ row }">
            <el-icon v-if="row.icon" style="margin-right: 5px;">
              <component :is="row.icon" />
            </el-icon>
            {{ row.menuName }}
          </template>
        </el-table-column>
        <el-table-column prop="menuCode" label="菜单编码" width="150" />
        <el-table-column prop="path" label="路由路径" min-width="150" />
        <el-table-column prop="component" label="组件路径" min-width="150" show-overflow-tooltip />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.menuType === 1 ? 'primary' : 'info'">
              {{ row.menuType === 1 ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleAddChild(row)">新增</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="menuTree"
            :props="{ label: 'menuName', value: 'id', children: 'children' }"
            placeholder="请选择上级菜单"
            clearable
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio :label="1">菜单</el-radio>
            <el-radio :label="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="菜单编码" prop="menuCode">
          <el-input v-model="form.menuCode" placeholder="请输入菜单编码" />
        </el-form-item>
        <el-form-item label="图标" v-if="form.menuType === 1">
          <el-input v-model="form.icon" placeholder="请输入图标名称（如：HomeFilled）" />
        </el-form-item>
        <el-form-item label="路由路径" prop="path" v-if="form.menuType === 1">
          <el-input v-model="form.path" placeholder="请输入路由路径（如：/system/user）" />
        </el-form-item>
        <el-form-item label="组件路径" v-if="form.menuType === 1">
          <el-input v-model="form.component" placeholder="请输入组件路径（如：system/User）" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
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
const menuList = ref([])
const menuTree = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增菜单')
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  parentId: 0,
  menuName: '',
  menuCode: '',
  path: '',
  component: '',
  icon: '',
  menuType: 1,
  sort: 0,
  status: 1
})

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuCode: [{ required: true, message: '请输入菜单编码', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

// 模拟数据
const mockMenus = [
  {
    id: 1,
    menuName: '系统管理',
    menuCode: 'system',
    path: '/system',
    component: '',
    icon: 'Setting',
    menuType: 1,
    sort: 1,
    status: 1,
    children: [
      {
        id: 2,
        menuName: '用户管理',
        menuCode: 'system:user',
        path: '/system/user',
        component: 'system/User',
        icon: 'User',
        menuType: 1,
        sort: 1,
        status: 1
      },
      {
        id: 3,
        menuName: '角色管理',
        menuCode: 'system:role',
        path: '/system/role',
        component: 'system/Role',
        icon: 'UserFilled',
        menuType: 1,
        sort: 2,
        status: 1
      },
      {
        id: 4,
        menuName: '菜单管理',
        menuCode: 'system:menu',
        path: '/system/menu',
        component: 'system/Menu',
        icon: 'Menu',
        menuType: 1,
        sort: 3,
        status: 1
      }
    ]
  },
  {
    id: 5,
    menuName: '医院管理',
    menuCode: 'hospital',
    path: '/hospital',
    component: '',
    icon: 'OfficeBuilding',
    menuType: 1,
    sort: 2,
    status: 1,
    children: [
      {
        id: 6,
        menuName: '医院列表',
        menuCode: 'hospital:list',
        path: '/hospital/list',
        component: 'hospital/List',
        icon: 'List',
        menuType: 1,
        sort: 1,
        status: 1
      }
    ]
  },
  {
    id: 7,
    menuName: '监控管理',
    menuCode: 'monitor',
    path: '/monitor',
    component: '',
    icon: 'Monitor',
    menuType: 1,
    sort: 3,
    status: 1,
    children: [
      {
        id: 8,
        menuName: '服务器监控',
        menuCode: 'monitor:server',
        path: '/monitor/server',
        component: 'monitor/Server',
        icon: 'Cpu',
        menuType: 1,
        sort: 1,
        status: 1
      },
      {
        id: 9,
        menuName: 'MySQL监控',
        menuCode: 'monitor:mysql',
        path: '/monitor/mysql',
        component: 'monitor/Mysql',
        icon: 'Coin',
        menuType: 1,
        sort: 2,
        status: 1
      },
      {
        id: 10,
        menuName: 'Tomcat监控',
        menuCode: 'monitor:tomcat',
        path: '/monitor/tomcat',
        component: 'monitor/Tomcat',
        icon: 'Box',
        menuType: 1,
        sort: 3,
        status: 1
      },
      {
        id: 11,
        menuName: '告警管理',
        menuCode: 'monitor:alert',
        path: '/monitor/alert',
        component: 'monitor/Alert',
        icon: 'Bell',
        menuType: 1,
        sort: 4,
        status: 1
      }
    ]
  },
  {
    id: 12,
    menuName: '数据大屏',
    menuCode: 'dashboard',
    path: '/dashboard',
    component: 'Dashboard',
    icon: 'DataLine',
    menuType: 1,
    sort: 0,
    status: 1
  }
]

// 获取菜单列表
const fetchMenuList = async () => {
  loading.value = true
  try {
    // 模拟数据
    menuList.value = mockMenus
    menuTree.value = [{ id: 0, menuName: '根目录', children: mockMenus }]
  } catch (error) {
    console.error('获取菜单列表失败', error)
  } finally {
    loading.value = false
  }
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增菜单'
  resetForm()
  dialogVisible.value = true
}

// 新增子菜单
const handleAddChild = (row) => {
  isEdit.value = false
  dialogTitle.value = '新增子菜单'
  resetForm()
  form.parentId = row.id
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑菜单'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除菜单 "${row.menuName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // await deleteMenu(row.id)
      ElMessage.success('删除成功')
      fetchMenuList()
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
      // await updateMenu(form)
      ElMessage.success('修改成功')
    } else {
      // await addMenu(form)
      ElMessage.success('新增成功')
    }
    
    dialogVisible.value = false
    fetchMenuList()
  } catch (error) {
    console.error('提交失败', error)
  }
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.parentId = 0
  form.menuName = ''
  form.menuCode = ''
  form.path = ''
  form.component = ''
  form.icon = ''
  form.menuType = 1
  form.sort = 0
  form.status = 1
}

// 初始化
fetchMenuList()
</script>

<style scoped>
.menu-manage {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
