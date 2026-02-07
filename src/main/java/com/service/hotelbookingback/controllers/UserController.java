package com.service.hotelbookingback.controllers;

import com.service.hotelbookingback.dtos.ApiResponse;
import com.service.hotelbookingback.dtos.BookingDTO;
import com.service.hotelbookingback.dtos.UserDTO;
import com.service.hotelbookingback.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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


    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateOwnAccount(@RequestBody UserDTO userDTO){
        return ResponseEntity.ok(userService.updateOwnAccount(userDTO));
    }

    @DeleteMapping("/delete")
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


    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getMyBookingHistory(){
        return ResponseEntity.ok(userService.getMyBookingHistory());
    }
}
