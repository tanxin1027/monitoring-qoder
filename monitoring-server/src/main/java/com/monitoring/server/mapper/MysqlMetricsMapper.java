package com.monitoring.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monitoring.server.entity.MysqlMetrics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * MySQL监控指标Mapper
 */
@Mapper
public interface MysqlMetricsMapper extends BaseMapper<MysqlMetrics> {

    @Select("SELECT * FROM mon_mysql_metrics WHERE server_id = #{serverId} " +
            "ORDER BY collect_time DESC LIMIT #{limit}")
    List<MysqlMetrics> selectRecentByServerId(@Param("serverId") Long serverId, 
                                               @Param("limit") Integer limit);

    @Select("SELECT * FROM mon_mysql_metrics WHERE server_id = #{serverId} " +
            "AND collect_time >= DATE_SUB(NOW(), INTERVAL #{hours} HOUR) " +
            "ORDER BY collect_time ASC")
    List<MysqlMetrics> selectByTimeRange(@Param("serverId") Long serverId, 
                                         @Param("hours") Integer hours);
}
