package com.service.hotelbookingback.controllers;

import com.service.hotelbookingback.dtos.*;
import com.service.hotelbookingback.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/account/update")
    public ResponseEntity<ApiResponse<UserDTO>> updateOwnAccount(@RequestBody UpdateProfileRequest request){
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    @PutMapping("/account/change-password")
    public ResponseEntity<ApiResponse> changePassword(@RequestBody ChangePasswordRequest request){
        return ResponseEntity.ok(userService.changePassword(request));
    }

    @DeleteMapping("/account/delete")
    public ResponseEntity<ApiResponse> deleteOwnAccount(){
        return ResponseEntity.ok(userService.deleteOwnAccount());
    }

    @GetMapping("/account")
    public ResponseEntity<ApiResponse<UserDTO>> getOwnAccountDetails(){
        try {
            ApiResponse<UserDTO> response = userService.getOwnAccountDetails();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<UserDTO>builder()
                            .status(401)
                            .message("Authentication required")
                            .build());
        }
    }

    @PutMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserDTO>> updateAvatar(
            @PathVariable Long id,
            @RequestPart("avatar") MultipartFile avatar) {
        return ResponseEntity.ok(userService.updateAvatar(id, avatar));
    }

    // Remove user avatar (set to default)
    @DeleteMapping("/{id}/avatar")
    public ResponseEntity<ApiResponse<UserDTO>> removeAvatar(@PathVariable Long id) {
        return ResponseEntity.ok(userService.removeAvatar(id));
    }
}
