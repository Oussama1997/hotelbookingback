package com.service.hotelbookingback.services.impl;

import com.service.hotelbookingback.dtos.*;
import com.service.hotelbookingback.dtos.auth.*;
import com.service.hotelbookingback.entities.Booking;
import com.service.hotelbookingback.entities.User;
import com.service.hotelbookingback.enums.EmailTemplate;
import com.service.hotelbookingback.enums.ImageType;
import com.service.hotelbookingback.enums.UserRole;
import com.service.hotelbookingback.exceptions.InvalidCredentialException;
import com.service.hotelbookingback.exceptions.NotFoundException;
import com.service.hotelbookingback.repositories.BookingRepository;
import com.service.hotelbookingback.repositories.UserRepository;
import com.service.hotelbookingback.security.JwtUtils;
import com.service.hotelbookingback.services.EmailService;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final ModelMapper modelMapper;
    private final BookingRepository bookingRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;

    @Value("${jwt.expiration}")
    private String expiration;

    @Override
    public AuthResponse registerUser(RegistrationRequest registrationRequest) {
        User userToSave = User.builder()
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .username(registrationRequest.getUsername())
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .phoneNumber(registrationRequest.getPhoneNumber())
                .role(UserRole.CUSTOMER)
                .isActive(Boolean.TRUE)
                .build();
        User savedUser = userRepository.save(userToSave);
        sendNotifWel(savedUser);
        return AuthResponse.builder()
                .status(200)
                .message("User Created Successfully")
                .build();

    }

    private void sendNotifWel(User user){
        //send notification via email
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", user.getLastName() + " " + user.getFirstName());

        emailService.sendTemplateEmail(user.getEmail(),
                EmailTemplate.BOOKING_CREATION, vars);// sending email
    }

    @Override
    public AuthResponse loginUser(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(()-> new NotFoundException("Email Not Found"));
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialException("Password Doesn't Match");
        }
        // Generate tokens
        String accessToken = jwtUtils.generateToken(user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());
        // Calculate expiration details
        Date expirationDate = jwtUtils.getExpirationDateFromToken(accessToken);
        long expiresIn = (expirationDate.getTime() - System.currentTimeMillis()) / 1000;
        String expiresAt = expirationDate.toInstant().toString();
        return AuthResponse.builder()
                .status(200)
                .message("User Logged In Successfully")
                .role(user.getRole())
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .expiresAt(expiresAt)
                .isActive(user.isActive())
                .timestamp(LocalDateTime.now())
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
    public ApiResponse<UserDTO> updateProfile(UpdateProfileRequest request) {
        User existingUser = getCurrentLoggedInUser();
        log.info("Inside updateOwnAccount");
        if (request.getFirstName() != null) existingUser.setFirstName(request.getFirstName());
        if (request.getLastName() != null) existingUser.setLastName(request.getLastName());
        if (request.getUsername() != null) existingUser.setUsername(request.getUsername());
        if (request.getPhoneNumber() != null) existingUser.setPhoneNumber(request.getPhoneNumber());
        userRepository.save(existingUser);
        UserDTO userDTO = modelMapper.map(existingUser, UserDTO.class);
        return ApiResponse.<UserDTO>builder()
                .status(200)
                .message("User Updated Successfully")
                .data(userDTO)
                .build();
    }

    @Override
    public ApiResponse changePassword(ChangePasswordRequest request) {
        User existingUser = getCurrentLoggedInUser();
        log.info("Inside changePassword");
        String errorMessage = "Invalid data";
        if (request.getConfirmPassword() != null && !request.getConfirmPassword().isEmpty()
            && request.getNewPassword() != null && !request.getNewPassword().isEmpty()
            && request.getCurrentPassword() != null && !request.getCurrentPassword().isEmpty()
            && request.getConfirmPassword().equals(request.getNewPassword())
        ) {
            if (request.getCurrentPassword().equals(passwordEncoder.encode(existingUser.getPassword()))) {
                existingUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
                userRepository.save(existingUser);
                return ApiResponse.builder()
                        .status(200)
                        .message("Password Updated Successfully")
                        .build();
            } else {
                errorMessage = "Invalid Current Password";
            }
        }
        return ApiResponse.builder()
                .status(404)
                .message(errorMessage)
                .build();
    }

    @Override
    public ApiResponse deleteOwnAccount() {
        User existingUser = getCurrentLoggedInUser();
        existingUser.setActive(false);
        userRepository.save(existingUser);
        return ApiResponse.builder()
                .status(200)
                .message("User Deleted Successfully")
                .build();
    }

    @Override
    public ApiResponse<List<BookingDTO>> getMyBookingHistory() {
        User user = getCurrentLoggedInUser();
        List<Booking> bookingList = bookingRepository.findByUserId(user.getId());
        List<BookingDTO> bookingDTOList = modelMapper.map(bookingList, new TypeToken<List<BookingDTO>>(){}.getType());
        return ApiResponse.<List<BookingDTO>>builder()
                .status(200)
                .message("Success")
                .data(bookingDTOList)
                .build();
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        try {
            String oldRefreshToken = request.getRefreshToken();
            // Validate refresh token
            if (!jwtUtils.isTokenValid(oldRefreshToken)) {
                throw new InvalidCredentialException("Invalid or expired refresh token");
            }
            // Extract email from refresh token
            String email = jwtUtils.getUsernameFromToken(oldRefreshToken);
            // Verify user exists and is active
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            if (!user.isActive()) {
                throw new InvalidCredentialException("User account is not active");
            }
            // Generate new access token
            String newAccessToken = jwtUtils.generateToken(email);
            // Generate new refresh token (optional: you can keep the same one until it expires)
            String newRefreshToken = jwtUtils.generateRefreshToken(email);
            // Calculate expiration details
            Date expirationDate = jwtUtils.getExpirationDateFromToken(newAccessToken);
            long expiresIn = (expirationDate.getTime() - System.currentTimeMillis()) / 1000;
            String expiresAt = expirationDate.toInstant().toString();
            return AuthResponse.builder()
                    .status(200)
                    .message("Token refreshed successfully")
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(expiresIn)
                    .expiresAt(expiresAt)
                    .isActive(user.isActive())
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            throw new InvalidCredentialException("Failed to refresh token: " + e.getMessage());
        }
    }

    @Override
    public User getCurrentLoggedInUser(){
        log.info("Inside getOwnAccountDetails");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new NotFoundException("User Not Found"));
        return user;
    }

    @Override
    public ApiResponse<UserDTO> updateAvatar(Long userId, MultipartFile avatar) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete old avatar if exists
        if (user.getAvatarFileName() != null && !user.getAvatarFileName().isEmpty()) {
            fileStorageService.deleteFile(user.getAvatarFileName(), ImageType.USER_AVATAR);
        }

        // Store new avatar
        String fileName = fileStorageService.storeUserAvatar(avatar);
        user.setAvatarFileName(fileName);

        User savedUser = userRepository.save(user);
        return ApiResponse.<UserDTO>builder()
                .status(200)
                .message("Success")
                .data(convertToResponseDTO(savedUser))
                .build();
    }

    @Override
    public ApiResponse<UserDTO> removeAvatar(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete avatar file if exists
        if (user.getAvatarFileName() != null && !user.getAvatarFileName().isEmpty()) {
            fileStorageService.deleteFile(user.getAvatarFileName(), ImageType.USER_AVATAR);
        }

        // Set to null to use default avatar
        user.setAvatarFileName(null);

        User savedUser = userRepository.save(user);
        return ApiResponse.<UserDTO>builder()
                .status(200)
                .message("User Deleted Successfully")
                .data(convertToResponseDTO(savedUser))
                .build();
    }

    private UserDTO convertToResponseDTO(User user) {
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        return userDTO;
    }
}