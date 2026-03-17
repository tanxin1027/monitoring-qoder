package com.monitoring.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.monitoring.server.dto.LoginRequest;
import com.monitoring.server.dto.LoginResponse;
import com.monitoring.server.entity.User;
import com.monitoring.server.mapper.UserMapper;
import com.monitoring.server.service.UserService;
import com.monitoring.server.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = getByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        List<String> roles = getUserRoles(user.getId());
        List<String> permissions = getUserPermissions(user.getId());

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .realName(user.getRealName())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        return userMapper.selectRoleCodesByUserId(userId);
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        return userMapper.selectMenuCodesByUserId(userId);
    }

    @Override
    public List<Long> getUserHospitalIds(Long userId) {
        return userMapper.selectHospitalIdsByUserId(userId);
    }
}
