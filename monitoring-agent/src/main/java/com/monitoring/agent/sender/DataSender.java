package com.monitoring.agent.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitoring.agent.config.AgentProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据发送器
 */
@Slf4j
@Component
public class DataSender {

    private final AgentProperties agentProperties;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public DataSender(AgentProperties agentProperties, ObjectMapper objectMapper, WebClient webClient) {
        this.agentProperties = agentProperties;
        this.objectMapper = objectMapper;
        this.webClient = webClient;
    }

    /**
     * 发送监控数据到服务端
     */
    public void sendMetrics(Map<String, Object> serverMetrics, 
                           Map<String, Object> mysqlMetrics,
                           Map<String, Object> tomcatMetrics) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("serverIp", getServerIp());
            payload.put("agentKey", agentProperties.getIdentity().getAgentKey());
            payload.put("collectTime", LocalDateTime.now().toString());

            // 服务器指标
            if (serverMetrics != null && !serverMetrics.isEmpty()) {
                Map<String, Object> serverData = new HashMap<>();
                serverData.put("cpuUsage", serverMetrics.get("cpuUsage"));
                serverData.put("memoryUsage", serverMetrics.get("memoryUsage"));
                serverData.put("memoryUsed", serverMetrics.get("memoryUsed"));
                serverData.put("memoryTotal", serverMetrics.get("memoryTotal"));
                serverData.put("diskUsage", serverMetrics.get("diskUsage"));
                serverData.put("diskUsed", serverMetrics.get("diskUsed"));
                serverData.put("diskTotal", serverMetrics.get("diskTotal"));
                serverData.put("loadAverage1", serverMetrics.get("loadAverage1"));
                serverData.put("loadAverage5", serverMetrics.get("loadAverage5"));
                serverData.put("loadAverage15", serverMetrics.get("loadAverage15"));
                serverData.put("processCount", serverMetrics.get("processCount"));
                serverData.put("collectTime", LocalDateTime.now());
                payload.put("serverMetrics", serverData);
            }

            // MySQL指标
            if (mysqlMetrics != null && !mysqlMetrics.isEmpty()) {
                Map<String, Object> mysqlData = new HashMap<>();
                mysqlData.put("mysqlVersion", mysqlMetrics.get("mysqlVersion"));
                mysqlData.put("port", mysqlMetrics.get("port"));
                mysqlData.put("qps", mysqlMetrics.get("qps"));
                mysqlData.put("tps", mysqlMetrics.get("tps"));
                mysqlData.put("connections", mysqlMetrics.get("connections"));
                mysqlData.put("maxConnections", mysqlMetrics.get("maxConnections"));
                mysqlData.put("threadsRunning", mysqlMetrics.get("threadsRunning"));
                mysqlData.put("threadsConnected", mysqlMetrics.get("threadsConnected"));
                mysqlData.put("slowQueries", mysqlMetrics.get("slowQueries"));
                mysqlData.put("collectTime", LocalDateTime.now());
                payload.put("mysqlMetrics", mysqlData);
            }

            // Tomcat指标
            if (tomcatMetrics != null && !tomcatMetrics.isEmpty()) {
                Map<String, Object> tomcatData = new HashMap<>();
                tomcatData.put("tomcatVersion", tomcatMetrics.getOrDefault("protocol", "Unknown"));
                tomcatData.put("port", tomcatMetrics.get("port"));
                tomcatData.put("maxThreads", tomcatMetrics.get("maxThreads"));
                tomcatData.put("currentThreads", tomcatMetrics.get("currentThreads"));
                tomcatData.put("busyThreads", tomcatMetrics.get("busyThreads"));
                tomcatData.put("connectionCount", tomcatMetrics.get("connectionCount"));
                tomcatData.put("bytesReceived", tomcatMetrics.get("bytesReceived"));
                tomcatData.put("bytesSent", tomcatMetrics.get("bytesSent"));
                tomcatData.put("requestCount", tomcatMetrics.get("requestCount"));
                tomcatData.put("errorCount", tomcatMetrics.get("errorCount"));
                tomcatData.put("collectTime", LocalDateTime.now());
                payload.put("tomcatMetrics", tomcatData);
            }

            String jsonPayload = objectMapper.writeValueAsString(payload);
            log.debug("发送数据: {}", jsonPayload);

            webClient.post()
                    .uri(agentProperties.getServer().getReportUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info("数据发送成功"))
                    .doOnError(error -> log.error("数据发送失败: {}", error.getMessage()))
                    .subscribe();

        } catch (Exception e) {
            log.error("发送数据失败", e);
        }
    }

    /**
     * 发送心跳
     */
    public void sendHeartbeat() {
        try {
            String url = agentProperties.getServer().getHeartbeatUrl() + 
                    "?serverIp=" + getServerIp() + 
                    "&agentKey=" + agentProperties.getIdentity().getAgentKey();

            webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.debug("心跳发送成功"))
                    .doOnError(error -> log.error("心跳发送失败: {}", error.getMessage()))
                    .subscribe();

        } catch (Exception e) {
            log.error("发送心跳失败", e);
        }
    }

    /**
     * 获取服务器IP
     */
    private String getServerIp() {
        String serverIp = agentProperties.getIdentity().getServerIp();
        if (serverIp == null || serverIp.isEmpty()) {
            try {
                serverIp = java.net.InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                serverIp = "127.0.0.1";
            }
        }
        return serverIp;
    }
}
