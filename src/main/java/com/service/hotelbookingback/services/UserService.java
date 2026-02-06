package com.service.hotelbookingback.services;

import com.service.hotelbookingback.dtos.*;
import com.service.hotelbookingback.entities.User;
import com.service.hotelbookingback.exceptions.NotFoundException;

import java.util.List;

public interface UserService {

    AuthResponse registerUser(RegistrationRequest registrationRequest);

    AuthResponse loginUser(LoginRequest loginRequest);

    ApiResponse<List<UserDTO>> getAllUsers();

    ApiResponse<UserDTO> getOwnAccountDetails();

    ApiResponse<UserDTO> getUserDetails(String email);

    public User getCurrentLoggedInUser();

    ApiResponse updateOwnAccount(UserDTO userDTO);

    ApiResponse deleteOwnAccount();

    ApiResponse<List<BookingDTO>> getMyBookingHistory();
}
