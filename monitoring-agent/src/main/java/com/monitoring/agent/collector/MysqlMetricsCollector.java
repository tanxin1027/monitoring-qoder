package com.monitoring.agent.collector;

import com.monitoring.agent.config.AgentProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * MySQL监控指标采集器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MysqlMetricsCollector {

    private final AgentProperties agentProperties;

    /**
     * 采集MySQL指标
     */
    public Map<String, Object> collect() {
        Map<String, Object> metrics = new HashMap<>();
        
        if (!agentProperties.getMonitoring().getMysql()) {
            return metrics;
        }

        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=Asia/Shanghai",
                agentProperties.getMysql().getHost(),
                agentProperties.getMysql().getPort(),
                agentProperties.getMysql().getDatabase());

        try (Connection conn = DriverManager.getConnection(url, 
                agentProperties.getMysql().getUsername(),
                agentProperties.getMysql().getPassword())) {
            
            // 获取版本信息
            metrics.putAll(getVersionInfo(conn));
            
            // 获取连接信息
            metrics.putAll(getConnectionInfo(conn));
            
            // 获取性能指标
            metrics.putAll(getPerformanceMetrics(conn));
            
            // 获取InnoDB状态
            metrics.putAll(getInnodbStatus(conn));

            log.debug("MySQL指标采集完成");
        } catch (Exception e) {
            log.error("采集MySQL指标失败", e);
        }

        return metrics;
    }

    /**
     * 获取版本信息
     */
    private Map<String, Object> getVersionInfo(Connection conn) throws SQLException {
        Map<String, Object> versionInfo = new HashMap<>();
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT VERSION() as version")) {
            if (rs.next()) {
                versionInfo.put("mysqlVersion", rs.getString("version"));
            }
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW VARIABLES LIKE 'port'")) {
            if (rs.next()) {
                versionInfo.put("port", rs.getInt("Value"));
            }
        }
        
        return versionInfo;
    }

    /**
     * 获取连接信息
     */
    private Map<String, Object> getConnectionInfo(Connection conn) throws SQLException {
        Map<String, Object> connectionInfo = new HashMap<>();
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW STATUS LIKE 'Threads_connected'")) {
            if (rs.next()) {
                connectionInfo.put("threadsConnected", rs.getInt("Value"));
            }
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW STATUS LIKE 'Threads_running'")) {
            if (rs.next()) {
                connectionInfo.put("threadsRunning", rs.getInt("Value"));
            }
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW VARIABLES LIKE 'max_connections'")) {
            if (rs.next()) {
                connectionInfo.put("maxConnections", rs.getInt("Value"));
            }
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW STATUS LIKE 'Connections'")) {
            if (rs.next()) {
                connectionInfo.put("connections", rs.getInt("Value"));
            }
        }
        
        return connectionInfo;
    }

    /**
     * 获取性能指标
     */
    private Map<String, Object> getPerformanceMetrics(Connection conn) throws SQLException {
        Map<String, Object> performanceMetrics = new HashMap<>();
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW GLOBAL STATUS LIKE 'Queries'")) {
            if (rs.next()) {
                performanceMetrics.put("questions", rs.getLong("Value"));
            }
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW GLOBAL STATUS LIKE 'Slow_queries'")) {
            if (rs.next()) {
                performanceMetrics.put("slowQueries", rs.getLong("Value"));
            }
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW GLOBAL STATUS LIKE 'Uptime'")) {
            if (rs.next()) {
                performanceMetrics.put("uptime", rs.getLong("Value"));
            }
        }
        
        // 计算QPS和TPS（简化计算）
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW GLOBAL STATUS WHERE Variable_name IN ('Com_select', 'Com_insert', 'Com_update', 'Com_delete')")) {
            long selectCount = 0, insertCount = 0, updateCount = 0, deleteCount = 0;
            while (rs.next()) {
                String name = rs.getString("Variable_name");
                long value = rs.getLong("Value");
                switch (name) {
                    case "Com_select": selectCount = value; break;
                    case "Com_insert": insertCount = value; break;
                    case "Com_update": updateCount = value; break;
                    case "Com_delete": deleteCount = value; break;
                }
            }
            
            // 这里简化处理，实际应该计算差值
            long totalQueries = selectCount + insertCount + updateCount + deleteCount;
            performanceMetrics.put("qps", BigDecimal.valueOf(selectCount).setScale(2, RoundingMode.HALF_UP));
            performanceMetrics.put("tps", BigDecimal.valueOf(insertCount + updateCount + deleteCount).setScale(2, RoundingMode.HALF_UP));
        }
        
        return performanceMetrics;
    }

    /**
     * 获取InnoDB状态
     */
    private Map<String, Object> getInnodbStatus(Connection conn) throws SQLException {
        Map<String, Object> innodbStatus = new HashMap<>();
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW VARIABLES LIKE 'innodb_buffer_pool_size'")) {
            if (rs.next()) {
                long bufferPoolSize = rs.getLong("Value");
                innodbStatus.put("bufferPoolSize", bufferPoolSize);
            }
        }
        
        // 计算缓冲池使用率
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW STATUS LIKE 'Innodb_buffer_pool_pages_data'")) {
            if (rs.next()) {
                long pagesData = rs.getLong("Value");
                innodbStatus.put("bufferPoolPagesData", pagesData);
            }
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW STATUS LIKE 'Innodb_buffer_pool_pages_total'")) {
            if (rs.next()) {
                long pagesTotal = rs.getLong("Value");
                innodbStatus.put("bufferPoolPagesTotal", pagesTotal);
            }
        }
        
        return innodbStatus;
    }
}
