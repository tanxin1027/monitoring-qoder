package com.monitoring.server.controller;

import com.monitoring.server.dto.Result;
import com.monitoring.server.mapper.*;
import com.monitoring.server.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据可视化控制器
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ServerMapper serverMapper;
    private final ServerMetricsMapper serverMetricsMapper;
    private final MysqlMetricsMapper mysqlMetricsMapper;
    private final TomcatMetricsMapper tomcatMetricsMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 服务器统计
        Integer totalServers = serverMapper.selectCount(null);
        Integer onlineServers = serverMapper.countByStatus(1);
        Integer offlineServers = totalServers - onlineServers;
        
        statistics.put("totalServers", totalServers);
        statistics.put("onlineServers", onlineServers);
        statistics.put("offlineServers", offlineServers);
        
        // 告警统计（示例）
        statistics.put("warningCount", 0);
        statistics.put("criticalCount", 0);
        
        return Result.success(statistics);
    }

    /**
     * 获取服务器列表
     */
    @GetMapping("/servers")
    public Result<List<Map<String, Object>>> getServerList(
            @RequestParam(required = false) Long hospitalId) {
        List<Server> servers;
        if (hospitalId != null) {
            servers = serverMapper.selectByHospitalId(hospitalId);
        } else {
            servers = serverMapper.selectList(null);
        }

        List<Map<String, Object>> result = servers.stream().map(server -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", server.getId());
            map.put("serverName", server.getServerName());
            map.put("serverIp", server.getServerIp());
            map.put("osType", server.getOsType());
            map.put("status", checkServerStatus(server.getId()));
            map.put("lastHeartbeat", server.getLastHeartbeat());
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    /**
     * 获取服务器实时性能数据
     */
    @GetMapping("/server/{serverId}/metrics")
    public Result<Map<String, Object>> getServerMetrics(@PathVariable Long serverId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取最新的性能指标
        List<ServerMetrics> metrics = serverMetricsMapper.selectRecentByServerId(serverId, 1);
        if (!metrics.isEmpty()) {
            ServerMetrics latest = metrics.get(0);
            result.put("cpuUsage", latest.getCpuUsage());
            result.put("memoryUsage", latest.getMemoryUsage());
            result.put("diskUsage", latest.getDiskUsage());
            result.put("loadAverage", latest.getLoadAverage1());
            result.put("processCount", latest.getProcessCount());
            result.put("collectTime", latest.getCollectTime());
        }

        return Result.success(result);
    }

    /**
     * 获取服务器历史性能数据
     */
    @GetMapping("/server/{serverId}/metrics/history")
    public Result<List<ServerMetrics>> getServerMetricsHistory(
            @PathVariable Long serverId,
            @RequestParam(defaultValue = "24") Integer hours) {
        List<ServerMetrics> metrics = serverMetricsMapper.selectByTimeRange(serverId, hours);
        return Result.success(metrics);
    }

    /**
     * 获取MySQL监控数据
     */
    @GetMapping("/server/{serverId}/mysql")
    public Result<Map<String, Object>> getMysqlMetrics(@PathVariable Long serverId) {
        Map<String, Object> result = new HashMap<>();
        
        List<MysqlMetrics> metrics = mysqlMetricsMapper.selectRecentByServerId(serverId, 1);
        if (!metrics.isEmpty()) {
            MysqlMetrics latest = metrics.get(0);
            result.put("version", latest.getMysqlVersion());
            result.put("connections", latest.getConnections());
            result.put("maxConnections", latest.getMaxConnections());
            result.put("qps", latest.getQps());
            result.put("tps", latest.getTps());
            result.put("threadsRunning", latest.getThreadsRunning());
            result.put("threadsConnected", latest.getThreadsConnected());
            result.put("slowQueries", latest.getSlowQueries());
        }

        return Result.success(result);
    }

    /**
     * 获取Tomcat监控数据
     */
    @GetMapping("/server/{serverId}/tomcat")
    public Result<Map<String, Object>> getTomcatMetrics(@PathVariable Long serverId) {
        Map<String, Object> result = new HashMap<>();
        
        List<TomcatMetrics> metrics = tomcatMetricsMapper.selectRecentByServerId(serverId, 1);
        if (!metrics.isEmpty()) {
            TomcatMetrics latest = metrics.get(0);
            result.put("version", latest.getTomcatVersion());
            result.put("maxThreads", latest.getMaxThreads());
            result.put("currentThreads", latest.getCurrentThreads());
            result.put("busyThreads", latest.getBusyThreads());
            result.put("connectionCount", latest.getConnectionCount());
            result.put("requestCount", latest.getRequestCount());
            result.put("errorCount", latest.getErrorCount());
        }

        return Result.success(result);
    }

    /**
     * 检查服务器在线状态
     */
    private boolean checkServerStatus(Long serverId) {
        String key = "server:status:" + serverId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null;
    }
}
