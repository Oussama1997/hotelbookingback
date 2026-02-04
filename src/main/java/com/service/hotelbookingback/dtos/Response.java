package com.service.hotelbookingback.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.service.hotelbookingback.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    // Generic
    private int status;
    private String message;

    // For Login
    private String token;
    private UserRole role;
    private boolean isActive;
    private String expirationTime;

    // User Data
    private UserDTO user;
    private List<UserDTO> users;

    // Booking Data
    private BookingDTO booking;
    private List<BookingDTO> bookings;

    // Room Data
    private RoomDTO room;
    private List<RoomDTO> rooms;

    // Room payments
    private String transactionId;
    private PaymentDTO payment;
    private List<PaymentDTO> payments;

    // Room Notification
    private NotificationDTO notification;
    private List<NotificationDTO> notifications;

    private final LocalDateTime timestamp = LocalDateTime.now();

}
