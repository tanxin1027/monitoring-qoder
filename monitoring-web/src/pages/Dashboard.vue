<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="statistics-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon server-total">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalServers || 0 }}</div>
              <div class="stat-label">服务器总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon server-online">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.onlineServers || 0 }}</div>
              <div class="stat-label">在线服务器</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon server-offline">
              <el-icon><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.offlineServers || 0 }}</div>
              <div class="stat-label">离线服务器</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon alert-count">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.warningCount || 0 }}</div>
              <div class="stat-label">告警数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>服务器状态分布</span>
            </div>
          </template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>资源使用率趋势</span>
            </div>
          </template>
          <div ref="lineChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 服务器列表 -->
    <el-card class="server-list-card">
      <template #header>
        <div class="card-header">
          <span>服务器列表</span>
          <el-button type="primary" size="small" @click="refreshData">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </div>
      </template>
      <el-table :data="serverList" stripe style="width: 100%">
        <el-table-column prop="serverName" label="服务器名称" min-width="150" />
        <el-table-column prop="serverIp" label="IP地址" min-width="130" />
        <el-table-column prop="osType" label="操作系统" min-width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="CPU" width="100">
          <template #default="{ row }">
            <el-progress 
              :percentage="row.cpuUsage || 0" 
              :status="getProgressStatus(row.cpuUsage)"
            />
          </template>
        </el-table-column>
        <el-table-column label="内存" width="100">
          <template #default="{ row }">
            <el-progress 
              :percentage="row.memoryUsage || 0" 
              :status="getProgressStatus(row.memoryUsage)"
            />
          </template>
        </el-table-column>
        <el-table-column label="磁盘" width="100">
          <template #default="{ row }">
            <el-progress 
              :percentage="row.diskUsage || 0" 
              :status="getProgressStatus(row.diskUsage)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="lastHeartbeat" label="最后心跳" min-width="160">
          <template #default="{ row }">
            {{ formatTime(row.lastHeartbeat) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getStatistics, getServerList } from '@/api/dashboard'
import dayjs from 'dayjs'

const router = useRouter()
const statistics = ref({})
const serverList = ref([])
const pieChartRef = ref(null)
const lineChartRef = ref(null)
let pieChart = null
let lineChart = null
let refreshTimer = null

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const res = await getStatistics()
    statistics.value = res.data
    updatePieChart()
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

// 获取服务器列表
const fetchServerList = async () => {
  try {
    const res = await getServerList()
    serverList.value = res.data || []
  } catch (error) {
    console.error('获取服务器列表失败', error)
  }
}

// 初始化饼图
const initPieChart = () => {
  pieChart = echarts.init(pieChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '服务器状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 0, name: '在线', itemStyle: { color: '#67C23A' } },
          { value: 0, name: '离线', itemStyle: { color: '#F56C6C' } }
        ]
      }
    ]
  }
  pieChart.setOption(option)
}

// 更新饼图数据
const updatePieChart = () => {
  if (!pieChart) return
  pieChart.setOption({
    series: [{
      data: [
        { value: statistics.value.onlineServers || 0, name: '在线', itemStyle: { color: '#67C23A' } },
        { value: statistics.value.offlineServers || 0, name: '离线', itemStyle: { color: '#F56C6C' } }
      ]
    }]
  })
}

// 初始化折线图
const initLineChart = () => {
  lineChart = echarts.init(lineChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['CPU使用率', '内存使用率', '磁盘使用率']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00']
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [
      {
        name: 'CPU使用率',
        type: 'line',
        smooth: true,
        data: [30, 35, 45, 60, 55, 40, 35]
      },
      {
        name: '内存使用率',
        type: 'line',
        smooth: true,
        data: [50, 52, 55, 58, 60, 55, 52]
      },
      {
        name: '磁盘使用率',
        type: 'line',
        smooth: true,
        data: [70, 70, 71, 71, 72, 72, 73]
      }
    ]
  }
  lineChart.setOption(option)
}

// 获取进度条状态
const getProgressStatus = (value) => {
  if (value >= 90) return 'exception'
  if (value >= 70) return 'warning'
  return ''
}

// 格式化时间
const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

// 刷新数据
const refreshData = () => {
  fetchStatistics()
  fetchServerList()
}

// 查看详情
const viewDetail = (row) => {
  router.push(`/monitor/server?id=${row.id}`)
}

// 窗口大小改变时重新渲染图表
const handleResize = () => {
  pieChart?.resize()
  lineChart?.resize()
}

onMounted(() => {
  fetchStatistics()
  fetchServerList()
  initPieChart()
  initLineChart()
  window.addEventListener('resize', handleResize)
  
  // 自动刷新
  refreshTimer = setInterval(() => {
    fetchStatistics()
    fetchServerList()
  }, 30000)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  lineChart?.dispose()
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.dashboard {
  padding-bottom: 20px;
}

.statistics-row {
  margin-bottom: 20px;
}

.stat-card {
  :deep(.el-card__body) {
    padding: 20px;
  }
}

.stat-item {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  margin-right: 15px;
}

.server-total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.server-online {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.server-offline {
  background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%);
}

.alert-count {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  :deep(.el-card__body) {
    padding: 20px;
  }
}

.chart-container {
  height: 300px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.server-list-card {
  :deep(.el-card__body) {
    padding: 0;
  }
}
</style>
