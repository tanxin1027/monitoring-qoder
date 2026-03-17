package com.monitoring.server.controller;

import com.monitoring.server.dto.AgentDataRequest;
import com.monitoring.server.dto.Result;
import com.monitoring.server.service.AgentDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Agent数据接收控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentDataService agentDataService;

    /**
     * 接收Agent上报的数据
     */
    @PostMapping("/report")
    public Result<Void> receiveAgentData(@RequestBody AgentDataRequest request) {
        try {
            agentDataService.receiveAgentData(request);
            return Result.success();
        } catch (Exception e) {
            log.error("接收Agent数据失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * Agent心跳检测
     */
    @GetMapping("/heartbeat")
    public Result<String> heartbeat(@RequestParam String serverIp, 
                                    @RequestParam String agentKey) {
        boolean valid = agentDataService.validateAgentKey(serverIp, agentKey);
        if (valid) {
            return Result.success("pong");
        } else {
            return Result.error(401, "认证失败");
        }
    }
}
