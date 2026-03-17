package com.monitoring.agent.collector;

import com.monitoring.agent.config.AgentProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Tomcat监控指标采集器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TomcatMetricsCollector {

    private final AgentProperties agentProperties;

    /**
     * 采集Tomcat指标
     */
    public Map<String, Object> collect() {
        Map<String, Object> metrics = new HashMap<>();
        
        if (!agentProperties.getMonitoring().getTomcat()) {
            return metrics;
        }

        try {
            // 尝试通过JMX连接Tomcat
            String jmxUrl = String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi",
                    agentProperties.getTomcat().getHost(),
                    agentProperties.getTomcat().getPort());
            
            JMXServiceURL serviceUrl = new JMXServiceURL(jmxUrl);
            Map<String, Object> env = new HashMap<>();
            
            // 如果配置了认证信息
            if (agentProperties.getTomcat().getUsername() != null && 
                agentProperties.getTomcat().getPassword() != null) {
                String[] credentials = new String[] {
                    agentProperties.getTomcat().getUsername(),
                    agentProperties.getTomcat().getPassword()
                };
                env.put(JMXConnector.CREDENTIALS, credentials);
            }

            try (JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceUrl, env)) {
                MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();
                
                // 获取线程池信息
                metrics.putAll(getThreadPoolInfo(mbsc));
                
                // 获取连接器信息
                metrics.putAll(getConnectorInfo(mbsc));
                
                // 获取全局请求处理器信息
                metrics.putAll(getGlobalRequestProcessorInfo(mbsc));

                log.debug("Tomcat指标采集完成");
            }
        } catch (Exception e) {
            log.error("采集Tomcat指标失败", e);
        }

        return metrics;
    }

    /**
     * 获取线程池信息
     */
    private Map<String, Object> getThreadPoolInfo(MBeanServerConnection mbsc) throws Exception {
        Map<String, Object> threadPoolInfo = new HashMap<>();
        
        Set<ObjectName> threadPools = mbsc.queryNames(
            new ObjectName("Catalina:type=ThreadPool,*"), null);
        
        for (ObjectName threadPool : threadPools) {
            Integer maxThreads = (Integer) mbsc.getAttribute(threadPool, "maxThreads");
            Integer currentThreads = (Integer) mbsc.getAttribute(threadPool, "currentThreadCount");
            Integer busyThreads = (Integer) mbsc.getAttribute(threadPool, "currentThreadsBusy");
            String name = threadPool.getKeyProperty("name");
            
            if (name != null && name.contains("http")) {
                threadPoolInfo.put("maxThreads", maxThreads);
                threadPoolInfo.put("currentThreads", currentThreads);
                threadPoolInfo.put("busyThreads", busyThreads);
            }
        }
        
        return threadPoolInfo;
    }

    /**
     * 获取连接器信息
     */
    private Map<String, Object> getConnectorInfo(MBeanServerConnection mbsc) throws Exception {
        Map<String, Object> connectorInfo = new HashMap<>();
        
        Set<ObjectName> connectors = mbsc.queryNames(
            new ObjectName("Catalina:type=Connector,*"), null);
        
        for (ObjectName connector : connectors) {
            String protocol = (String) mbsc.getAttribute(connector, "protocol");
            Integer port = (Integer) mbsc.getAttribute(connector, "port");
            
            if (protocol != null && protocol.toLowerCase().contains("http")) {
                connectorInfo.put("port", port);
                connectorInfo.put("protocol", protocol);
            }
        }
        
        return connectorInfo;
    }

    /**
     * 获取全局请求处理器信息
     */
    private Map<String, Object> getGlobalRequestProcessorInfo(MBeanServerConnection mbsc) throws Exception {
        Map<String, Object> processorInfo = new HashMap<>();
        
        Set<ObjectName> processors = mbsc.queryNames(
            new ObjectName("Catalina:type=GlobalRequestProcessor,*"), null);
        
        for (ObjectName processor : processors) {
            String name = processor.getKeyProperty("name");
            if (name != null && name.contains("http")) {
                Long bytesReceived = (Long) mbsc.getAttribute(processor, "bytesReceived");
                Long bytesSent = (Long) mbsc.getAttribute(processor, "bytesSent");
                Integer requestCount = (Integer) mbsc.getAttribute(processor, "requestCount");
                Integer errorCount = (Integer) mbsc.getAttribute(processor, "errorCount");
                Long processingTime = (Long) mbsc.getAttribute(processor, "processingTime");
                Long maxTime = (Long) mbsc.getAttribute(processor, "maxTime");
                
                processorInfo.put("bytesReceived", BigDecimal.valueOf(bytesReceived));
                processorInfo.put("bytesSent", BigDecimal.valueOf(bytesSent));
                processorInfo.put("requestCount", requestCount);
                processorInfo.put("errorCount", errorCount);
                processorInfo.put("processingTime", BigDecimal.valueOf(processingTime));
                processorInfo.put("maxTime", BigDecimal.valueOf(maxTime));
            }
        }
        
        return processorInfo;
    }

    /**
     * 获取Tomcat版本信息（通过HTTP请求）
     */
    public String getTomcatVersion() {
        try {
            WebClient webClient = WebClient.builder()
                .baseUrl("http://" + agentProperties.getTomcat().getHost() + ":" + agentProperties.getTomcat().getPort())
                .build();
            
            // 这里简化处理，实际应该解析HTML或调用特定接口
            return "Unknown";
        } catch (Exception e) {
            log.error("获取Tomcat版本失败", e);
            return "Unknown";
        }
    }
}
