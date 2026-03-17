package com.monitoring.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monitoring.server.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT r.role_code FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1 AND r.deleted = 0")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    @Select("SELECT m.menu_code FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = 1 AND m.deleted = 0")
    List<String> selectMenuCodesByUserId(@Param("userId") Long userId);

    @Select("SELECT h.id FROM sys_hospital h " +
            "INNER JOIN sys_user_hospital uh ON h.id = uh.hospital_id " +
            "WHERE uh.user_id = #{userId} AND h.status = 1 AND h.deleted = 0")
    List<Long> selectHospitalIdsByUserId(@Param("userId") Long userId);
}
