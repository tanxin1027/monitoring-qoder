package com.monitoring.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务器实体类
 */
@Data
@TableName("mon_server")
public class Server {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long hospitalId;

    private String serverName;

    private String serverIp;

    private String osType;

    private String osVersion;

    private Integer cpuCores;

    private Long memorySize;

    private Long diskSize;

    private String agentVersion;

    private LocalDateTime lastHeartbeat;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
