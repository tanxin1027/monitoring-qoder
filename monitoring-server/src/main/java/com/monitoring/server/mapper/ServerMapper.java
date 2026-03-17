package com.monitoring.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monitoring.server.entity.Server;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 服务器Mapper
 */
@Mapper
public interface ServerMapper extends BaseMapper<Server> {

    @Select("SELECT * FROM mon_server WHERE server_ip = #{serverIp} AND deleted = 0")
    Server selectByServerIp(@Param("serverIp") String serverIp);

    @Select("SELECT * FROM mon_server WHERE hospital_id = #{hospitalId} AND deleted = 0")
    List<Server> selectByHospitalId(@Param("hospitalId") Long hospitalId);

    @Select("SELECT COUNT(*) FROM mon_server WHERE status = #{status} AND deleted = 0")
    Integer countByStatus(@Param("status") Integer status);
}
