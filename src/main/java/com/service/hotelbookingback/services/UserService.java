package com.service.hotelbookingback.services;

import com.service.hotelbookingback.dtos.*;
import com.service.hotelbookingback.entities.User;

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
}
