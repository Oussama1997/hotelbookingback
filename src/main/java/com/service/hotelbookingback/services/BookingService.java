package com.service.hotelbookingback.services;

import com.service.hotelbookingback.dtos.BookingDTO;
import com.service.hotelbookingback.dtos.ApiResponse;

public interface BookingService {
    ApiResponse getAllBookings();
    ApiResponse createBooking(BookingDTO bookingDTO);
    ApiResponse findBookingByReferenceNo(String  bookingReference);
    ApiResponse updateBooking(BookingDTO bookingDTO);
}
