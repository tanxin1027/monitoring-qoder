package com.monitoring.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Agent配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "agent")
public class AgentProperties {

    private ServerProperties server;
    private IdentityProperties identity;
    private CollectionProperties collection;
    private MonitoringProperties monitoring;
    private MysqlProperties mysql;
    private TomcatProperties tomcat;

    @Data
    public static class ServerProperties {
        private String url;
        private String reportUrl;
        private String heartbeatUrl;
    }

    @Data
    public static class IdentityProperties {
        private String serverIp;
        private String agentKey;
    }

    @Data
    public static class CollectionProperties {
        private Integer interval;
        private Integer heartbeatInterval;
    }

    @Data
    public static class MonitoringProperties {
        private Boolean server;
        private Boolean mysql;
        private Boolean tomcat;
    }

    @Data
    public static class MysqlProperties {
        private String host;
        private Integer port;
        private String username;
        private String password;
        private String database;
    }

    @Data
    public static class TomcatProperties {
        private String host;
        private Integer port;
        private String username;
        private String password;
    }
}
