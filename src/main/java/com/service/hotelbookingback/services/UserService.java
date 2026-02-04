package com.service.hotelbookingback.services;

import com.service.hotelbookingback.dtos.LoginRequest;
import com.service.hotelbookingback.dtos.RegistrationRequest;
import com.service.hotelbookingback.dtos.Response;
import com.service.hotelbookingback.dtos.UserDTO;
import com.service.hotelbookingback.entities.User;
import com.service.hotelbookingback.exceptions.InvalidCredentialException;
import com.service.hotelbookingback.exceptions.NotFoundException;

public interface UserService {

    Response registerUser(RegistrationRequest registrationRequest);

    Response loginUser(LoginRequest loginRequest);

    Response getAllUsers();

    Response getOwnAccountDetails() throws NotFoundException;

    User getCurrentLoggedInUser();

    Response updateOwnAccount(UserDTO userDTO);

    Response deleteOwnAccount();

    Response getMyBookingHistory();
}
