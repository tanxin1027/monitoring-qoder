<template>
  <div class="server-monitor">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>服务器监控</span>
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

      <!-- 实时指标卡片 -->
      <el-row :gutter="20" class="metrics-row">
        <el-col :span="6">
          <div class="metric-card">
            <div class="metric-title">CPU 使用率</div>
            <div class="metric-value" :class="getValueClass(currentMetrics.cpuUsage)">
              {{ currentMetrics.cpuUsage || 0 }}%
            </div>
            <el-progress 
              :percentage="currentMetrics.cpuUsage || 0" 
              :status="getProgressStatus(currentMetrics.cpuUsage)"
              :stroke-width="10"
            />
          </div>
        </el-col>
        <el-col :span="6">
          <div class="metric-card">
            <div class="metric-title">内存 使用率</div>
            <div class="metric-value" :class="getValueClass(currentMetrics.memoryUsage)">
              {{ currentMetrics.memoryUsage || 0 }}%
            </div>
            <el-progress 
              :percentage="currentMetrics.memoryUsage || 0" 
              :status="getProgressStatus(currentMetrics.memoryUsage)"
              :stroke-width="10"
            />
            <div class="metric-detail">
              {{ formatSize(currentMetrics.memoryUsed) }} / {{ formatSize(currentMetrics.memoryTotal) }}
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="metric-card">
            <div class="metric-title">磁盘 使用率</div>
            <div class="metric-value" :class="getValueClass(currentMetrics.diskUsage)">
              {{ currentMetrics.diskUsage || 0 }}%
            </div>
            <el-progress 
              :percentage="currentMetrics.diskUsage || 0" 
              :status="getProgressStatus(currentMetrics.diskUsage)"
              :stroke-width="10"
            />
            <div class="metric-detail">
              {{ formatSize(currentMetrics.diskUsed) }} / {{ formatSize(currentMetrics.diskTotal) }}
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="metric-card">
            <div class="metric-title">系统负载</div>
            <div class="load-values">
              <div class="load-item">
                <span class="load-label">1分钟</span>
                <span class="load-value">{{ currentMetrics.loadAverage1 || 0 }}</span>
              </div>
              <div class="load-item">
                <span class="load-label">5分钟</span>
                <span class="load-value">{{ currentMetrics.loadAverage5 || 0 }}</span>
              </div>
              <div class="load-item">
                <span class="load-label">15分钟</span>
                <span class="load-value">{{ currentMetrics.loadAverage15 || 0 }}</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 历史趋势图表 -->
      <el-row :gutter="20" class="chart-row">
        <el-col :span="24">
          <div class="chart-container">
            <div class="chart-header">
              <span class="chart-title">历史性能趋势</span>
              <el-radio-group v-model="timeRange" size="small" @change="fetchHistoryData">
                <el-radio-button label="1">1小时</el-radio-button>
                <el-radio-button label="6">6小时</el-radio-button>
                <el-radio-button label="24">24小时</el-radio-button>
                <el-radio-button label="168">7天</el-radio-button>
              </el-radio-group>
            </div>
            <div ref="historyChartRef" class="history-chart"></div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { getServerList, getServerMetrics, getServerMetricsHistory } from '@/api/dashboard'
import { ElMessage } from 'element-plus'

const serverList = ref([])
const selectedServer = ref(null)
const currentMetrics = ref({})
const timeRange = ref('24')
const historyChartRef = ref(null)
let historyChart = null
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

// 获取实时指标
const fetchCurrentMetrics = async () => {
  if (!selectedServer.value) return
  try {
    const res = await getServerMetrics(selectedServer.value)
    currentMetrics.value = res.data || {}
  } catch (error) {
    console.error('获取实时指标失败', error)
  }
}

// 获取历史数据
const fetchHistoryData = async () => {
  if (!selectedServer.value) return
  try {
    const res = await getServerMetricsHistory(selectedServer.value, parseInt(timeRange.value))
    const data = res.data || []
    updateHistoryChart(data)
  } catch (error) {
    console.error('获取历史数据失败', error)
  }
}

// 初始化历史图表
const initHistoryChart = () => {
  historyChart = echarts.init(historyChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
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
      data: []
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
        data: [],
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '内存使用率',
        type: 'line',
        smooth: true,
        data: [],
        itemStyle: { color: '#67C23A' }
      },
      {
        name: '磁盘使用率',
        type: 'line',
        smooth: true,
        data: [],
        itemStyle: { color: '#E6A23C' }
      }
    ]
  }
  historyChart.setOption(option)
}

// 更新历史图表
const updateHistoryChart = (data) => {
  if (!historyChart) return
  
  const times = data.map(item => item.collectTime?.substring(11, 16) || '')
  const cpuData = data.map(item => parseFloat(item.cpuUsage) || 0)
  const memoryData = data.map(item => parseFloat(item.memoryUsage) || 0)
  const diskData = data.map(item => parseFloat(item.diskUsage) || 0)
  
  historyChart.setOption({
    xAxis: {
      data: times
    },
    series: [
      { data: cpuData },
      { data: memoryData },
      { data: diskData }
    ]
  })
}

// 获取进度条状态
const getProgressStatus = (value) => {
  if (!value) return ''
  if (value >= 90) return 'exception'
  if (value >= 70) return 'warning'
  return ''
}

// 获取数值样式类
const getValueClass = (value) => {
  if (!value) return ''
  if (value >= 90) return 'danger'
  if (value >= 70) return 'warning'
  return 'normal'
}

// 格式化大小
const formatSize = (value) => {
  if (!value) return '-'
  const num = parseFloat(value)
  if (num > 1024 * 1024) {
    return (num / 1024 / 1024).toFixed(2) + ' GB'
  } else if (num > 1024) {
    return (num / 1024).toFixed(2) + ' MB'
  }
  return num.toFixed(2) + ' MB'
}

// 刷新数据
const refreshData = () => {
  fetchCurrentMetrics()
  fetchHistoryData()
  ElMessage.success('数据已刷新')
}

// 监听服务器选择变化
watch(selectedServer, () => {
  fetchCurrentMetrics()
  fetchHistoryData()
})

// 窗口大小改变
const handleResize = () => {
  historyChart?.resize()
}

onMounted(() => {
  fetchServerList()
  initHistoryChart()
  window.addEventListener('resize', handleResize)
  
  // 自动刷新
  refreshTimer = setInterval(() => {
    fetchCurrentMetrics()
  }, 30000)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  historyChart?.dispose()
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.server-monitor {
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

.metrics-row {
  margin-bottom: 20px;
}

.metric-card {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  text-align: center;
}

.metric-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.metric-value {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 10px;
}

.metric-value.normal {
  color: #67C23A;
}

.metric-value.warning {
  color: #E6A23C;
}

.metric-value.danger {
  color: #F56C6C;
}

.metric-detail {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.load-values {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
}

.load-item {
  text-align: center;
}

.load-label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.load-value {
  display: block;
  font-size: 20px;
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

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.history-chart {
  height: 350px;
}
</style>
