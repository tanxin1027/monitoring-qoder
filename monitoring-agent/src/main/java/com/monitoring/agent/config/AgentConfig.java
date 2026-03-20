package com.monitoring.agent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Agent 配置类
 */
@Configuration
public class AgentConfig {

    @Bean
    public WebClient webClient(AgentProperties agentProperties) {
        return WebClient.builder()
                .baseUrl(agentProperties.getServer().getUrl())
                .build();
    }
}
