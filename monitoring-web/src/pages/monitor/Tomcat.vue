<template>
  <div class="tomcat-monitor">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>Tomcat 监控</span>
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

      <!-- Tomcat状态卡片 -->
      <el-row :gutter="20" class="status-row">
        <el-col :span="6">
          <div class="status-card">
            <div class="status-icon blue">
              <el-icon size="32"><Collection /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-value">{{ tomcatMetrics.maxThreads || 0 }}</div>
              <div class="status-label">最大线程数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-card">
            <div class="status-icon green">
              <el-icon size="32"><Loading /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-value">{{ tomcatMetrics.currentThreads || 0 }}</div>
              <div class="status-label">当前线程</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-card">
            <div class="status-icon orange">
              <el-icon size="32"><SetUp /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-value">{{ tomcatMetrics.busyThreads || 0 }}</div>
              <div class="status-label">忙碌线程</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-card">
            <div class="status-icon purple">
              <el-icon size="32"><Link /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-value">{{ tomcatMetrics.connectionCount || 0 }}</div>
              <div class="status-label">连接数</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 请求统计 -->
      <el-row :gutter="20" class="request-row">
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>
              <span>请求统计</span>
            </template>
            <div class="request-stats">
              <div class="request-item">
                <div class="request-icon success">
                  <el-icon size="24"><CircleCheck /></el-icon>
                </div>
                <div class="request-info">
                  <div class="request-value">{{ tomcatMetrics.requestCount || 0 }}</div>
                  <div class="request-label">总请求数</div>
                </div>
              </div>
              <div class="request-item">
                <div class="request-icon error">
                  <el-icon size="24"><CircleClose /></el-icon>
                </div>
                <div class="request-info">
                  <div class="request-value">{{ tomcatMetrics.errorCount || 0 }}</div>
                  <div class="request-label">错误数</div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>
              <span>流量统计</span>
            </template>
            <div class="traffic-stats">
              <div class="traffic-item">
                <div class="traffic-label">接收流量</div>
                <div class="traffic-value">{{ formatBytes(tomcatMetrics.bytesReceived) }}</div>
              </div>
              <div class="traffic-item">
                <div class="traffic-label">发送流量</div>
                <div class="traffic-value">{{ formatBytes(tomcatMetrics.bytesSent) }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 线程池使用率图表 -->
      <el-row :gutter="20" class="chart-row">
        <el-col :span="12">
          <div class="chart-container">
            <div class="chart-title">线程池使用率</div>
            <div ref="threadPoolChartRef" class="chart"></div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="chart-container">
            <div class="chart-title">请求成功率</div>
            <div ref="successRateChartRef" class="chart"></div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { getServerList, getTomcatMetrics } from '@/api/dashboard'
import { ElMessage } from 'element-plus'

const serverList = ref([])
const selectedServer = ref(null)
const tomcatMetrics = ref({})
const threadPoolChartRef = ref(null)
const successRateChartRef = ref(null)
let threadPoolChart = null
let successRateChart = null
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

// 获取Tomcat指标
const fetchTomcatMetrics = async () => {
  if (!selectedServer.value) return
  try {
    const res = await getTomcatMetrics(selectedServer.value)
    tomcatMetrics.value = res.data || {}
    updateCharts()
  } catch (error) {
    console.error('获取Tomcat指标失败', error)
  }
}

// 初始化线程池图表
const initThreadPoolChart = () => {
  threadPoolChart = echarts.init(threadPoolChartRef.value)
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
        name: '线程状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{c}'
        },
        data: [
          { value: 0, name: '忙碌线程', itemStyle: { color: '#F56C6C' } },
          { value: 0, name: '空闲线程', itemStyle: { color: '#67C23A' } },
          { value: 0, name: '可用线程', itemStyle: { color: '#909399' } }
        ]
      }
    ]
  }
  threadPoolChart.setOption(option)
}

// 初始化成功率图表
const initSuccessRateChart = () => {
  successRateChart = echarts.init(successRateChartRef.value)
  const option = {
    tooltip: {
      formatter: '{a} <br/>{b} : {c}%'
    },
    series: [
      {
        name: '请求成功率',
        type: 'gauge',
        min: 0,
        max: 100,
        detail: {
          formatter: '{value}%',
          fontSize: 24
        },
        data: [{ value: 100, name: '成功率' }],
        axisLine: {
          lineStyle: {
            color: [
              [0.8, '#F56C6C'],
              [0.95, '#E6A23C'],
              [1, '#67C23A']
            ]
          }
        }
      }
    ]
  }
  successRateChart.setOption(option)
}

// 更新图表
const updateCharts = () => {
  if (!threadPoolChart || !successRateChart) return
  
  const maxThreads = tomcatMetrics.value.maxThreads || 1
  const currentThreads = tomcatMetrics.value.currentThreads || 0
  const busyThreads = tomcatMetrics.value.busyThreads || 0
  const idleThreads = Math.max(0, currentThreads - busyThreads)
  const availableThreads = Math.max(0, maxThreads - currentThreads)
  
  threadPoolChart.setOption({
    series: [{
      data: [
        { value: busyThreads, name: '忙碌线程', itemStyle: { color: '#F56C6C' } },
        { value: idleThreads, name: '空闲线程', itemStyle: { color: '#67C23A' } },
        { value: availableThreads, name: '可用线程', itemStyle: { color: '#909399' } }
      ]
    }]
  })
  
  // 计算成功率
  const requestCount = tomcatMetrics.value.requestCount || 0
  const errorCount = tomcatMetrics.value.errorCount || 0
  const successRate = requestCount > 0 
    ? Math.round(((requestCount - errorCount) / requestCount) * 100) 
    : 100
  
  successRateChart.setOption({
    series: [{
      data: [{ value: successRate, name: '成功率' }]
    }]
  })
}

// 格式化字节
const formatBytes = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 刷新数据
const refreshData = () => {
  fetchTomcatMetrics()
  ElMessage.success('数据已刷新')
}

// 监听服务器选择变化
watch(selectedServer, () => {
  fetchTomcatMetrics()
})

// 窗口大小改变
const handleResize = () => {
  threadPoolChart?.resize()
  successRateChart?.resize()
}

onMounted(() => {
  fetchServerList()
  initThreadPoolChart()
  initSuccessRateChart()
  window.addEventListener('resize', handleResize)
  
  // 自动刷新
  refreshTimer = setInterval(() => {
    fetchTomcatMetrics()
  }, 30000)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  threadPoolChart?.dispose()
  successRateChart?.dispose()
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.tomcat-monitor {
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
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  color: #fff;
}

.status-icon.blue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.status-icon.green {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.status-icon.orange {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.status-icon.purple {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.status-info {
  flex: 1;
}

.status-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.status-label {
  font-size: 14px;
  color: #909399;
}

.request-row {
  margin-bottom: 20px;
}

.request-stats {
  display: flex;
  justify-content: space-around;
}

.request-item {
  display: flex;
  align-items: center;
}

.request-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  color: #fff;
}

.request-icon.success {
  background-color: #67C23A;
}

.request-icon.error {
  background-color: #F56C6C;
}

.request-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.request-label {
  font-size: 14px;
  color: #909399;
}

.traffic-stats {
  padding: 10px 0;
}

.traffic-item {
  display: flex;
  justify-content: space-between;
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
}

.traffic-item:last-child {
  border-bottom: none;
}

.traffic-label {
  color: #606266;
}

.traffic-value {
  font-weight: bold;
  color: #409EFF;
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
