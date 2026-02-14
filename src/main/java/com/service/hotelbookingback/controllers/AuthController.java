package com.service.hotelbookingback.controllers;

import com.service.hotelbookingback.dtos.auth.AuthResponse;
import com.service.hotelbookingback.dtos.auth.LoginRequest;
import com.service.hotelbookingback.dtos.auth.RefreshTokenRequest;
import com.service.hotelbookingback.dtos.auth.RegistrationRequest;
import com.service.hotelbookingback.exceptions.InvalidCredentialException;
import com.service.hotelbookingback.exceptions.NotFoundException;
import com.service.hotelbookingback.services.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody @Valid RegistrationRequest request){
        return ResponseEntity.ok(userService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody @Valid LoginRequest request){
        return ResponseEntity.ok(userService.loginUser(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse response = userService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (InvalidCredentialException | NotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .status(401)
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.builder()
                            .status(500)
                            .message("Internal server error")
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }
}