<template>
  <div class="alert-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>告警管理</span>
          <el-button type="primary" @click="refreshData">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </div>
      </template>

      <!-- 筛选条件 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="告警级别">
          <el-select v-model="queryForm.level" placeholder="全部" clearable style="width: 120px;">
            <el-option label="警告" value="1" />
            <el-option label="严重" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="告警状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="未处理" value="0" />
            <el-option label="已处理" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务器">
          <el-select v-model="queryForm.serverId" placeholder="全部" clearable style="width: 150px;">
            <el-option
              v-for="server in serverList"
              :key="server.id"
              :label="server.serverName"
              :value="server.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 告警列表 -->
      <el-table :data="alertList" stripe style="width: 100%" v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="alertTitle" label="告警标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="serverName" label="服务器" width="150" />
        <el-table-column label="告警级别" width="100">
          <template #default="{ row }">
            <el-tag :type="row.alertLevel === 2 ? 'danger' : 'warning'">
              {{ row.alertLevel === 2 ? '严重' : '警告' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertType" label="告警类型" width="120" />
        <el-table-column prop="alertContent" label="告警内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'danger' : 'success'">
              {{ row.status === 0 ? '未处理' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="告警时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 0" 
              type="primary" 
              link 
              @click="handleAlert(row)"
            >
              处理
            </el-button>
            <el-button type="primary" link @click="viewDetail(row)">详情</el-button>
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

    <!-- 处理告警对话框 -->
    <el-dialog v-model="dialogVisible" title="处理告警" width="500px">
      <el-form :model="handleForm" label-width="80px">
        <el-form-item label="告警标题">
          <span>{{ currentAlert?.alertTitle }}</span>
        </el-form-item>
        <el-form-item label="告警内容">
          <span>{{ currentAlert?.alertContent }}</span>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="handleForm.remark"
            type="textarea"
            rows="4"
            placeholder="请输入处理备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getServerList } from '@/api/dashboard'

const serverList = ref([])
const alertList = ref([])
const loading = ref(false)
const total = ref(0)
const dialogVisible = ref(false)
const currentAlert = ref(null)

const queryForm = reactive({
  level: '',
  status: '',
  serverId: '',
  pageNum: 1,
  pageSize: 10
})

const handleForm = reactive({
  remark: ''
})

// 获取服务器列表
const fetchServerList = async () => {
  try {
    const res = await getServerList()
    serverList.value = res.data || []
  } catch (error) {
    console.error('获取服务器列表失败', error)
  }
}

// 获取告警列表
const fetchAlertList = async () => {
  loading.value = true
  try {
    // 这里应该调用实际的API
    // const res = await getAlertList(queryForm)
    // alertList.value = res.data.list || []
    // total.value = res.data.total || 0
    
    // 模拟数据
    alertList.value = [
      {
        id: 1,
        alertTitle: 'CPU使用率过高',
        serverName: '示例服务器-01',
        alertLevel: 2,
        alertType: 'CPU',
        alertContent: 'CPU使用率超过90%，当前为95%',
        status: 0,
        createTime: '2024-01-15 10:30:00'
      },
      {
        id: 2,
        alertTitle: '内存使用率警告',
        serverName: '示例服务器-01',
        alertLevel: 1,
        alertType: 'Memory',
        alertContent: '内存使用率超过80%，当前为85%',
        status: 0,
        createTime: '2024-01-15 09:15:00'
      },
      {
        id: 3,
        alertTitle: 'MySQL连接数过多',
        serverName: '示例服务器-01',
        alertLevel: 1,
        alertType: 'MySQL',
        alertContent: 'MySQL连接数接近最大值',
        status: 1,
        createTime: '2024-01-14 16:45:00'
      }
    ]
    total.value = alertList.value.length
  } catch (error) {
    console.error('获取告警列表失败', error)
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryForm.pageNum = 1
  fetchAlertList()
}

// 重置查询
const resetQuery = () => {
  queryForm.level = ''
  queryForm.status = ''
  queryForm.serverId = ''
  queryForm.pageNum = 1
  fetchAlertList()
}

// 处理告警
const handleAlert = (row) => {
  currentAlert.value = row
  handleForm.remark = ''
  dialogVisible.value = true
}

// 提交处理
const submitHandle = async () => {
  if (!handleForm.remark.trim()) {
    ElMessage.warning('请输入处理备注')
    return
  }
  
  try {
    // 这里应该调用实际的API
    // await handleAlertApi(currentAlert.value.id, handleForm.remark)
    ElMessage.success('告警已处理')
    dialogVisible.value = false
    fetchAlertList()
  } catch (error) {
    console.error('处理告警失败', error)
  }
}

// 查看详情
const viewDetail = (row) => {
  ElMessageBox.alert(
    `<div style="line-height: 1.8;">
      <p><strong>告警标题：</strong>${row.alertTitle}</p>
      <p><strong>服务器：</strong>${row.serverName}</p>
      <p><strong>告警级别：</strong>${row.alertLevel === 2 ? '严重' : '警告'}</p>
      <p><strong>告警类型：</strong>${row.alertType}</p>
      <p><strong>告警内容：</strong>${row.alertContent}</p>
      <p><strong>告警时间：</strong>${row.createTime}</p>
    </div>`,
    '告警详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确定'
    }
  )
}

// 刷新数据
const refreshData = () => {
  fetchAlertList()
  ElMessage.success('数据已刷新')
}

// 分页
const handleSizeChange = (val) => {
  queryForm.pageSize = val
  fetchAlertList()
}

const handleCurrentChange = (val) => {
  queryForm.pageNum = val
  fetchAlertList()
}

onMounted(() => {
  fetchServerList()
  fetchAlertList()
})
</script>

<style scoped>
.alert-manage {
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
