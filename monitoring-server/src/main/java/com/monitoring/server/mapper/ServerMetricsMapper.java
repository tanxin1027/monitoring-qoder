package com.monitoring.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monitoring.server.entity.ServerMetrics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 服务器性能指标Mapper
 */
@Mapper
public interface ServerMetricsMapper extends BaseMapper<ServerMetrics> {

    @Select("SELECT * FROM mon_server_metrics WHERE server_id = #{serverId} " +
            "ORDER BY collect_time DESC LIMIT #{limit}")
    List<ServerMetrics> selectRecentByServerId(@Param("serverId") Long serverId, 
                                                @Param("limit") Integer limit);

    @Select("SELECT * FROM mon_server_metrics WHERE server_id = #{serverId} " +
            "AND collect_time >= DATE_SUB(NOW(), INTERVAL #{hours} HOUR) " +
            "ORDER BY collect_time ASC")
    List<ServerMetrics> selectByTimeRange(@Param("serverId") Long serverId, 
                                          @Param("hours") Integer hours);
}
