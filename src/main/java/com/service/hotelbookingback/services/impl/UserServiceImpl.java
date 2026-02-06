package com.service.hotelbookingback.services.impl;

import com.service.hotelbookingback.dtos.*;
import com.service.hotelbookingback.entities.Booking;
import com.service.hotelbookingback.entities.User;
import com.service.hotelbookingback.enums.UserRole;
import com.service.hotelbookingback.exceptions.InvalidCredentialException;
import com.service.hotelbookingback.exceptions.NotFoundException;
import com.service.hotelbookingback.repositories.BookingRepository;
import com.service.hotelbookingback.repositories.UserRepository;
import com.service.hotelbookingback.security.JwtUtils;
import com.service.hotelbookingback.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final ModelMapper modelMapper;
    private final BookingRepository bookingRepository;

    @Value("${jwt.expiration}")
    private String expiration;


    @Override
    public AuthResponse registerUser(RegistrationRequest registrationRequest) {
        UserRole role = UserRole.CUSTOMER;
        if (registrationRequest.getRole() != null) {
            role = registrationRequest.getRole();
        }
        User userToSave = User.builder()
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .phoneNumber(registrationRequest.getPhoneNumber())
                .role(role)
                .isActive(Boolean.TRUE)
                .build();
        userRepository.save(userToSave);
        return AuthResponse.builder()
                .status(200)
                .message("User Created Successfully")
                .build();

    }

    @Override
    public AuthResponse loginUser(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(()-> new NotFoundException("Email Not Found"));
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialException("Password Doesn't Match");
        }
        String token = jwtUtils.generateToken(user.getEmail());
        return AuthResponse.builder()
                .status(200)
                .message("User Logged In Successfully")
                .role(user.getRole())
                .token(token)
                .isActive(user.isActive())
                .expirationTime(expiration)
                .build();
    }

    @Override
    public ApiResponse<List<UserDTO>> getAllUsers() {
        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<UserDTO> userDTOList = modelMapper.map(users, new TypeToken<List<UserDTO>>(){}.getType());
        return ApiResponse.<List<UserDTO>>builder()
                .status(200)
                .message("Success")
                .data(userDTOList)
                .build();
    }

    @Override
    public ApiResponse<UserDTO> getOwnAccountDetails() {
        User existingUser = getCurrentLoggedInUser();
        UserDTO userDTO = modelMapper.map(existingUser, UserDTO.class);
        return ApiResponse.<UserDTO>builder()
                .status(200)
                .message("Success")
                .data(userDTO)
                .build();
    }

    @Override
    public ApiResponse<UserDTO> getUserDetails(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new NotFoundException("User Not Found"));
        log.info("Inside getUserAccount User Email is {}", email);
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        return ApiResponse.<UserDTO>builder()
                .status(200)
                .message("Success")
                .data(userDTO)
                .build();
    }

    @Override
    public ApiResponse updateOwnAccount(UserDTO userDTO) {
        User existingUser = getCurrentLoggedInUser();
        log.info("Inside updateOwnAccount");
        if (userDTO.getEmail() != null) existingUser.setEmail(userDTO.getEmail());
        if (userDTO.getFirstName() != null) existingUser.setFirstName(userDTO.getFirstName());
        if (userDTO.getLastName() != null) existingUser.setLastName(userDTO.getLastName());
        if (userDTO.getPhoneNumber() != null) existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        userRepository.save(existingUser);
        return ApiResponse.builder()
                .status(200)
                .message("User Updated Successfully")
                .build();
    }

    @Override
    public ApiResponse deleteOwnAccount() {
        User user = getCurrentLoggedInUser();
        userRepository.delete(user);
        return ApiResponse.builder()
                .status(200)
                .message("User Deleted Successfully")
                .build();
    }

    @Override
    public ApiResponse getMyBookingHistory() {
        User user = getCurrentLoggedInUser();
        List<Booking> bookingList = bookingRepository.findByUserId(user.getId());
        List<BookingDTO> bookingDTOList = modelMapper.map(bookingList, new TypeToken<List<BookingDTO>>(){}.getType());
        return ApiResponse.builder()
                .status(200)
                .message("Success")
                .data(bookingDTOList)
                .build();
    }

    @Override
    public User getCurrentLoggedInUser(){
        log.info("Inside getOwnAccountDetails");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new NotFoundException("User Not Found"));
        return user;
    }
}