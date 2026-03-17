package com.monitoring.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务器性能指标实体类
 */
@Data
@TableName("mon_server_metrics")
public class ServerMetrics {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long serverId;

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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
