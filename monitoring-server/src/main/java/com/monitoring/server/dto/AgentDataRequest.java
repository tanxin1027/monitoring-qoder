package com.monitoring.server.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Agent上报数据DTO
 */
@Data
public class AgentDataRequest {

    private String serverIp;

    private String agentKey;

    private ServerMetricsData serverMetrics;

    private MysqlMetricsData mysqlMetrics;

    private TomcatMetricsData tomcatMetrics;

    @Data
    public static class ServerMetricsData {
        private BigDecimal cpuUsage;
        private BigDecimal memoryUsage;
        private BigDecimal memoryUsed;
        private BigDecimal memoryTotal;
        private BigDecimal diskUsage;
        private BigDecimal diskUsed;
        private BigDecimal diskTotal;
        private BigDecimal loadAverage1;
        private BigDecimal loadAverage5;
        private BigDecimal loadAverage15;
        private Integer processCount;
        private LocalDateTime collectTime;
    }

    @Data
    public static class MysqlMetricsData {
        private String mysqlVersion;
        private Integer port;
        private BigDecimal qps;
        private BigDecimal tps;
        private Integer connections;
        private Integer maxConnections;
        private Integer threadsRunning;
        private Integer threadsConnected;
        private BigDecimal slowQueries;
        private LocalDateTime collectTime;
    }

    @Data
    public static class TomcatMetricsData {
        private String tomcatVersion;
        private Integer port;
        private Integer maxThreads;
        private Integer currentThreads;
        private Integer busyThreads;
        private Integer connectionCount;
        private BigDecimal bytesReceived;
        private BigDecimal bytesSent;
        private Integer requestCount;
        private Integer errorCount;
        private LocalDateTime collectTime;
    }
}
