package com.monitoring.server.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 登录响应DTO
 */
@Data
@Builder
public class LoginResponse {

    private String token;

    private String username;

    private String realName;

    private List<String> roles;

    private List<String> permissions;
}
