package com.monitoring.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.monitoring.server.dto.LoginRequest;
import com.monitoring.server.dto.LoginResponse;
import com.monitoring.server.entity.User;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);

    /**
     * 获取用户角色列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 获取用户权限列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 获取用户可访问的医院ID列表
     */
    List<Long> getUserHospitalIds(Long userId);
}
