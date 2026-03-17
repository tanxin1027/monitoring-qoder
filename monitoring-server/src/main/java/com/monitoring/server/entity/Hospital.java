package com.monitoring.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 医院实体类
 */
@Data
@TableName("sys_hospital")
public class Hospital {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String hospitalName;

    private String hospitalCode;

    private String address;

    private String contactPerson;

    private String contactPhone;

    private String description;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
