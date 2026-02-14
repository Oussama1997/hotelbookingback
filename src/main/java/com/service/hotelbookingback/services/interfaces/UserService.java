package com.service.hotelbookingback.services.interfaces;

import com.service.hotelbookingback.dtos.*;
import com.service.hotelbookingback.dtos.auth.*;
import com.service.hotelbookingback.entities.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    AuthResponse registerUser(RegistrationRequest registrationRequest);

    AuthResponse loginUser(LoginRequest loginRequest);

    ApiResponse<List<UserDTO>> getAllUsers();

    ApiResponse<UserDTO> getOwnAccountDetails();

    ApiResponse<UserDTO> getUserDetails(String email);

    User getCurrentLoggedInUser();

    ApiResponse<UserDTO> updateProfile(UpdateProfileRequest request);

    ApiResponse changePassword(ChangePasswordRequest request);

    ApiResponse deleteOwnAccount();

    ApiResponse<List<BookingDTO>> getMyBookingHistory();

    AuthResponse refreshToken(RefreshTokenRequest request);

    ApiResponse<UserDTO> updateAvatar(Long userId, MultipartFile avatar);

    ApiResponse<UserDTO> removeAvatar(Long userId);
}
