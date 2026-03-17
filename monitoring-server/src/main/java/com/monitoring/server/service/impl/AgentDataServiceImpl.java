package com.monitoring.server.service.impl;

import com.monitoring.server.dto.AgentDataRequest;
import com.monitoring.server.entity.*;
import com.monitoring.server.mapper.*;
import com.monitoring.server.service.AgentDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Agent数据接收服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentDataServiceImpl implements AgentDataService {

    private final ServerMapper serverMapper;
    private final ServerMetricsMapper serverMetricsMapper;
    private final MysqlMetricsMapper mysqlMetricsMapper;
    private final TomcatMetricsMapper tomcatMetricsMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String AGENT_KEY_PREFIX = "agent:key:";
    private static final String SERVER_STATUS_PREFIX = "server:status:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveAgentData(AgentDataRequest request) {
        log.info("接收到Agent数据，服务器IP: {}", request.getServerIp());

        // 验证Agent
        if (!validateAgentKey(request.getServerIp(), request.getAgentKey())) {
            throw new RuntimeException("Agent密钥验证失败");
        }

        // 获取或创建服务器记录
        Server server = serverMapper.selectByServerIp(request.getServerIp());
        if (server == null) {
            server = new Server();
            server.setServerIp(request.getServerIp());
            server.setServerName("Server-" + request.getServerIp());
            server.setStatus(1);
            serverMapper.insert(server);
        }

        // 更新最后心跳时间
        server.setLastHeartbeat(LocalDateTime.now());
        serverMapper.updateById(server);

        // 保存服务器性能指标
        if (request.getServerMetrics() != null) {
            saveServerMetrics(server.getId(), request.getServerMetrics());
        }

        // 保存MySQL指标
        if (request.getMysqlMetrics() != null) {
            saveMysqlMetrics(server.getId(), request.getMysqlMetrics());
        }

        // 保存Tomcat指标
        if (request.getTomcatMetrics() != null) {
            saveTomcatMetrics(server.getId(), request.getTomcatMetrics());
        }

        // 更新Redis缓存
        updateServerStatusCache(server.getId());

        log.info("Agent数据处理完成，服务器ID: {}", server.getId());
    }

    @Override
    public boolean validateAgentKey(String serverIp, String agentKey) {
        // 从Redis获取Agent密钥进行验证
        String key = AGENT_KEY_PREFIX + serverIp;
        String storedKey = (String) redisTemplate.opsForValue().get(key);
        
        if (storedKey == null) {
            // 如果Redis中没有，从数据库验证
            Server server = serverMapper.selectByServerIp(serverIp);
            if (server != null) {
                // 这里可以实现更复杂的密钥验证逻辑
                return true;
            }
            return false;
        }
        
        return storedKey.equals(agentKey);
    }

    private void saveServerMetrics(Long serverId, AgentDataRequest.ServerMetricsData data) {
        ServerMetrics metrics = new ServerMetrics();
        metrics.setServerId(serverId);
        metrics.setCpuUsage(data.getCpuUsage());
        metrics.setMemoryUsage(data.getMemoryUsage());
        metrics.setMemoryUsed(data.getMemoryUsed());
        metrics.setMemoryTotal(data.getMemoryTotal());
        metrics.setDiskUsage(data.getDiskUsage());
        metrics.setDiskUsed(data.getDiskUsed());
        metrics.setDiskTotal(data.getDiskTotal());
        metrics.setLoadAverage1(data.getLoadAverage1());
        metrics.setLoadAverage5(data.getLoadAverage5());
        metrics.setLoadAverage15(data.getLoadAverage15());
        metrics.setProcessCount(data.getProcessCount());
        metrics.setCollectTime(data.getCollectTime());
        
        serverMetricsMapper.insert(metrics);
    }

    private void saveMysqlMetrics(Long serverId, AgentDataRequest.MysqlMetricsData data) {
        MysqlMetrics metrics = new MysqlMetrics();
        metrics.setServerId(serverId);
        metrics.setMysqlVersion(data.getMysqlVersion());
        metrics.setPort(data.getPort());
        metrics.setQps(data.getQps());
        metrics.setTps(data.getTps());
        metrics.setConnections(data.getConnections());
        metrics.setMaxConnections(data.getMaxConnections());
        metrics.setThreadsRunning(data.getThreadsRunning());
        metrics.setThreadsConnected(data.getThreadsConnected());
        metrics.setSlowQueries(data.getSlowQueries());
        metrics.setCollectTime(data.getCollectTime());
        
        mysqlMetricsMapper.insert(metrics);
    }

    private void saveTomcatMetrics(Long serverId, AgentDataRequest.TomcatMetricsData data) {
        TomcatMetrics metrics = new TomcatMetrics();
        metrics.setServerId(serverId);
        metrics.setTomcatVersion(data.getTomcatVersion());
        metrics.setPort(data.getPort());
        metrics.setMaxThreads(data.getMaxThreads());
        metrics.setCurrentThreads(data.getCurrentThreads());
        metrics.setBusyThreads(data.getBusyThreads());
        metrics.setConnectionCount(data.getConnectionCount());
        metrics.setBytesReceived(data.getBytesReceived());
        metrics.setBytesSent(data.getBytesSent());
        metrics.setRequestCount(data.getRequestCount());
        metrics.setErrorCount(data.getErrorCount());
        metrics.setCollectTime(data.getCollectTime());
        
        tomcatMetricsMapper.insert(metrics);
    }

    private void updateServerStatusCache(Long serverId) {
        String key = SERVER_STATUS_PREFIX + serverId;
        redisTemplate.opsForValue().set(key, System.currentTimeMillis(), 5, TimeUnit.MINUTES);
    }
}
