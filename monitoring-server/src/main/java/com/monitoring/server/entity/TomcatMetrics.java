package com.monitoring.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tomcat监控指标实体类
 */
@Data
@TableName("mon_tomcat_metrics")
public class TomcatMetrics {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long serverId;

    private String tomcatVersion;

    private Integer port;

    private Integer maxThreads;

    private Integer currentThreads;

    private Integer busyThreads;

    private Integer connectionCount;

    private BigDecimal bytesReceived;

    private BigDecimal bytesSent;

    private Integer requestCount;

    private Integer errorCount;

    private BigDecimal processingTime;

    private BigDecimal maxTime;

    private LocalDateTime collectTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
