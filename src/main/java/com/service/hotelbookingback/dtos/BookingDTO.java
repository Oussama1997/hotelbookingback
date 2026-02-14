package com.service.hotelbookingback.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.service.hotelbookingback.dtos.room.RoomDTO;
import com.service.hotelbookingback.enums.BookingStatus;
import com.service.hotelbookingback.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingDTO {

    private Long id;
    private UserDTO user;
    private RoomDTO room;
    private String roomNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int guests;
    private BigDecimal totalPrice;
    private String reference;
    private BookingStatus status;
    private String specialRequests;
    private LocalDateTime paymentDeadline;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
