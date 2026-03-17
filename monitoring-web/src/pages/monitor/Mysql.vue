<template>
  <div class="mysql-monitor">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>MySQL 监控</span>
          <div class="header-actions">
            <el-select v-model="selectedServer" placeholder="选择服务器" style="width: 200px; margin-right: 10px;">
              <el-option
                v-for="server in serverList"
                :key="server.id"
                :label="server.serverName"
                :value="server.id"
              />
            </el-select>
            <el-button type="primary" @click="refreshData">
              <el-icon><Refresh /></el-icon>刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- MySQL状态卡片 -->
      <el-row :gutter="20" class="status-row">
        <el-col :span="8">
          <div class="status-card">
            <div class="status-icon">
              <el-icon size="40" color="#409EFF"><Connection /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-title">连接信息</div>
              <div class="status-value">{{ mysqlMetrics.connections || 0 }} / {{ mysqlMetrics.maxConnections || 0 }}</div>
              <div class="status-label">当前连接 / 最大连接</div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="status-card">
            <div class="status-icon">
              <el-icon size="40" color="#67C23A"><TrendCharts /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-title">QPS / TPS</div>
              <div class="status-value">{{ mysqlMetrics.qps || 0 }} / {{ mysqlMetrics.tps || 0 }}</div>
              <div class="status-label">每秒查询 / 事务数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="status-card">
            <div class="status-icon">
              <el-icon size="40" color="#E6A23C"><Timer /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-title">慢查询</div>
              <div class="status-value">{{ mysqlMetrics.slowQueries || 0 }}</div>
              <div class="status-label">累计慢查询数</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 线程状态 -->
      <el-row :gutter="20" class="thread-row">
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>
              <span>线程状态</span>
            </template>
            <div class="thread-stats">
              <div class="thread-item">
                <span class="thread-label">运行线程</span>
                <el-tag type="success" size="large">{{ mysqlMetrics.threadsRunning || 0 }}</el-tag>
              </div>
              <div class="thread-item">
                <span class="thread-label">连接线程</span>
                <el-tag type="primary" size="large">{{ mysqlMetrics.threadsConnected || 0 }}</el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>
              <span>版本信息</span>
            </template>
            <div class="version-info">
              <div class="version-item">
                <span class="version-label">MySQL 版本</span>
                <span class="version-value">{{ mysqlMetrics.mysqlVersion || '-' }}</span>
              </div>
              <div class="version-item">
                <span class="version-label">端口号</span>
                <span class="version-value">{{ mysqlMetrics.port || '-' }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 连接使用率图表 -->
      <el-row :gutter="20" class="chart-row">
        <el-col :span="12">
          <div class="chart-container">
            <div class="chart-title">连接使用率</div>
            <div ref="connectionChartRef" class="chart"></div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="chart-container">
            <div class="chart-title">线程状态分布</div>
            <div ref="threadChartRef" class="chart"></div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { getServerList, getMysqlMetrics } from '@/api/dashboard'
import { ElMessage } from 'element-plus'

const serverList = ref([])
const selectedServer = ref(null)
const mysqlMetrics = ref({})
const connectionChartRef = ref(null)
const threadChartRef = ref(null)
let connectionChart = null
let threadChart = null
let refreshTimer = null

// 获取服务器列表
const fetchServerList = async () => {
  try {
    const res = await getServerList()
    serverList.value = res.data || []
    if (serverList.value.length > 0 && !selectedServer.value) {
      selectedServer.value = serverList.value[0].id
    }
  } catch (error) {
    console.error('获取服务器列表失败', error)
  }
}

// 获取MySQL指标
const fetchMysqlMetrics = async () => {
  if (!selectedServer.value) return
  try {
    const res = await getMysqlMetrics(selectedServer.value)
    mysqlMetrics.value = res.data || {}
    updateCharts()
  } catch (error) {
    console.error('获取MySQL指标失败', error)
  }
}

// 初始化连接使用率图表
const initConnectionChart = () => {
  connectionChart = echarts.init(connectionChartRef.value)
  const option = {
    tooltip: {
      formatter: '{a} <br/>{b} : {c}%'
    },
    series: [
      {
        name: '连接使用率',
        type: 'gauge',
        detail: {
          formatter: '{value}%',
          fontSize: 20
        },
        data: [{ value: 0, name: '使用率' }],
        axisLine: {
          lineStyle: {
            color: [
              [0.3, '#67C23A'],
              [0.7, '#E6A23C'],
              [1, '#F56C6C']
            ]
          }
        }
      }
    ]
  }
  connectionChart.setOption(option)
}

// 初始化线程状态图表
const initThreadChart = () => {
  threadChart = echarts.init(threadChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '线程状态',
        type: 'pie',
        radius: '70%',
        data: [
          { value: 0, name: '运行中', itemStyle: { color: '#67C23A' } },
          { value: 0, name: '已连接', itemStyle: { color: '#409EFF' } },
          { value: 0, name: '空闲', itemStyle: { color: '#909399' } }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  threadChart.setOption(option)
}

// 更新图表
const updateCharts = () => {
  if (!connectionChart || !threadChart) return
  
  // 更新连接使用率
  const connections = mysqlMetrics.value.connections || 0
  const maxConnections = mysqlMetrics.value.maxConnections || 1
  const usageRate = Math.round((connections / maxConnections) * 100)
  
  connectionChart.setOption({
    series: [{
      data: [{ value: usageRate, name: '使用率' }]
    }]
  })
  
  // 更新线程状态
  const threadsRunning = mysqlMetrics.value.threadsRunning || 0
  const threadsConnected = mysqlMetrics.value.threadsConnected || 0
  const threadsIdle = Math.max(0, threadsConnected - threadsRunning)
  
  threadChart.setOption({
    series: [{
      data: [
        { value: threadsRunning, name: '运行中', itemStyle: { color: '#67C23A' } },
        { value: threadsConnected, name: '已连接', itemStyle: { color: '#409EFF' } },
        { value: threadsIdle, name: '空闲', itemStyle: { color: '#909399' } }
      ]
    }]
  })
}

// 刷新数据
const refreshData = () => {
  fetchMysqlMetrics()
  ElMessage.success('数据已刷新')
}

// 监听服务器选择变化
watch(selectedServer, () => {
  fetchMysqlMetrics()
})

// 窗口大小改变
const handleResize = () => {
  connectionChart?.resize()
  threadChart?.resize()
}

onMounted(() => {
  fetchServerList()
  initConnectionChart()
  initThreadChart()
  window.addEventListener('resize', handleResize)
  
  // 自动刷新
  refreshTimer = setInterval(() => {
    fetchMysqlMetrics()
  }, 30000)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  connectionChart?.dispose()
  threadChart?.dispose()
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.mysql-monitor {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.status-row {
  margin-bottom: 20px;
}

.status-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.status-icon {
  margin-right: 20px;
}

.status-info {
  flex: 1;
}

.status-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.status-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.status-label {
  font-size: 12px;
  color: #909399;
}

.thread-row {
  margin-bottom: 20px;
}

.thread-stats {
  display: flex;
  justify-content: space-around;
}

.thread-item {
  text-align: center;
}

.thread-label {
  display: block;
  margin-bottom: 10px;
  color: #606266;
}

.version-info {
  padding: 10px 0;
}

.version-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.version-item:last-child {
  border-bottom: none;
}

.version-label {
  color: #606266;
}

.version-value {
  font-weight: bold;
  color: #303133;
}

.chart-row {
  margin-top: 20px;
}

.chart-container {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.chart-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 20px;
}

.chart {
  height: 300px;
}
</style>
