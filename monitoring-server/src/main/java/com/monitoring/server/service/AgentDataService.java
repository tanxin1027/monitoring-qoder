package com.monitoring.server.service;

import com.monitoring.server.dto.AgentDataRequest;

/**
 * Agent数据接收服务
 */
public interface AgentDataService {

    /**
     * 接收并处理Agent上报的数据
     */
    void receiveAgentData(AgentDataRequest request);

    /**
     * 验证Agent密钥
     */
    boolean validateAgentKey(String serverIp, String agentKey);
}
