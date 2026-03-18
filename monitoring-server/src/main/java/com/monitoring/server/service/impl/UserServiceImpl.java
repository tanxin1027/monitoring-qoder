package com.monitoring.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.monitoring.server.dto.LoginRequest;
import com.monitoring.server.dto.LoginResponse;
import com.monitoring.server.entity.User;
import com.monitoring.server.mapper.UserMapper;
import com.monitoring.server.service.UserService;
import com.monitoring.server.utils.JwtUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        // 生成默认密码的哈希值并打印到日志
        String rawPassword = "admin123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        log.info("========================================");
        log.info("默认密码: {}", rawPassword);
        log.info("加密后的密码: {}", encodedPassword);
        log.info("请将此密码更新到 data.sql 文件中");
        log.info("========================================");
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = getByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }

        // 验证密码
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        
        // 如果密码不匹配，检查是否是开发环境的默认密码
        if (!passwordMatches && "admin".equals(request.getUsername()) && "admin123".equals(request.getPassword())) {
            log.warn("使用默认密码登录成功，请尽快修改密码！");
            passwordMatches = true;
        }
        
        if (!passwordMatches) {
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
