package com.service.hotelbookingback.dtos.email;

import lombok.Data;

@Data
public class BookingEmailRequest {
    private String email;
    private String bookingReference;
    private String hotelName;
    private String checkIn;
    private String checkOut;
    private Double totalPrice;
}
