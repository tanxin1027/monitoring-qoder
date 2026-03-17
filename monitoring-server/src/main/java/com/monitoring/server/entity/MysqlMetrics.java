package com.monitoring.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MySQL监控指标实体类
 */
@Data
@TableName("mon_mysql_metrics")
public class MysqlMetrics {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long serverId;

    private String mysqlVersion;

    private Integer port;

    private BigDecimal qps;

    private BigDecimal tps;

    private Integer connections;

    private Integer maxConnections;

    private Integer threadsRunning;

    private Integer threadsConnected;

    private BigDecimal slowQueries;

    private BigDecimal questions;

    private BigDecimal uptime;

    private BigDecimal innodbBufferPoolUsage;

    private BigDecimal queryCacheHitRate;

    private LocalDateTime collectTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
