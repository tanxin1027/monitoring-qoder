import request from '@/utils/request'

// 获取统计信息
export const getStatistics = () => {
  return request({
    url: '/dashboard/statistics',
    method: 'get'
  })
}

// 获取服务器列表
export const getServerList = (params) => {
  return request({
    url: '/dashboard/servers',
    method: 'get',
    params
  })
}

// 获取服务器实时指标
export const getServerMetrics = (serverId) => {
  return request({
    url: `/dashboard/server/${serverId}/metrics`,
    method: 'get'
  })
}

// 获取服务器历史指标
export const getServerMetricsHistory = (serverId, hours = 24) => {
  return request({
    url: `/dashboard/server/${serverId}/metrics/history`,
    method: 'get',
    params: { hours }
  })
}

// 获取MySQL指标
export const getMysqlMetrics = (serverId) => {
  return request({
    url: `/dashboard/server/${serverId}/mysql`,
    method: 'get'
  })
}

// 获取Tomcat指标
export const getTomcatMetrics = (serverId) => {
  return request({
    url: `/dashboard/server/${serverId}/tomcat`,
    method: 'get'
  })
}
