package com.monitoring.agent.job;

import com.monitoring.agent.collector.MysqlMetricsCollector;
import com.monitoring.agent.collector.ServerMetricsCollector;
import com.monitoring.agent.collector.TomcatMetricsCollector;
import com.monitoring.agent.config.AgentProperties;
import com.monitoring.agent.sender.DataSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 指标采集定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsCollectionJob {

    private final AgentProperties agentProperties;
    private final ServerMetricsCollector serverMetricsCollector;
    private final MysqlMetricsCollector mysqlMetricsCollector;
    private final TomcatMetricsCollector tomcatMetricsCollector;
    private final DataSender dataSender;

    /**
     * 定时采集并发送指标数据
     */
    @Scheduled(fixedRateString = "${agent.collection.interval:60000}")
    public void collectAndSendMetrics() {
        log.info("开始采集监控指标...");

        Map<String, Object> serverMetrics = null;
        Map<String, Object> mysqlMetrics = null;
        Map<String, Object> tomcatMetrics = null;

        // 采集服务器指标
        if (agentProperties.getMonitoring().getServer()) {
            serverMetrics = serverMetricsCollector.collect();
        }

        // 采集MySQL指标
        if (agentProperties.getMonitoring().getMysql()) {
            mysqlMetrics = mysqlMetricsCollector.collect();
        }

        // 采集Tomcat指标
        if (agentProperties.getMonitoring().getTomcat()) {
            tomcatMetrics = tomcatMetricsCollector.collect();
        }

        // 发送数据
        dataSender.sendMetrics(serverMetrics, mysqlMetrics, tomcatMetrics);

        log.info("监控指标采集完成");
    }

    /**
     * 定时发送心跳
     */
    @Scheduled(fixedRateString = "${agent.collection.heartbeat-interval:30000}")
    public void sendHeartbeat() {
        log.debug("发送心跳...");
        dataSender.sendHeartbeat();
    }
}
