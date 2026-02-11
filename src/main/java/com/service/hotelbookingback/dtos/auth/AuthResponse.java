package com.service.hotelbookingback.dtos.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.service.hotelbookingback.dtos.UserDTO;
import com.service.hotelbookingback.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private int status;
    private String message;
    private String token;
    private UserRole role;
    private UserDTO user;
    private boolean isActive;
    private long expiresIn;      // Duration in seconds
    private String expiresAt;    // ISO date string of expiry
    private String refreshToken; // Add refresh token field
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}