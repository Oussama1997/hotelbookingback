package com.service.hotelbookingback.services;

import com.service.hotelbookingback.dtos.BookingDTO;
import com.service.hotelbookingback.dtos.ApiResponse;

import java.util.List;

public interface BookingService {
    ApiResponse<List<BookingDTO>> getAllBookings();
    ApiResponse<BookingDTO> createBooking(BookingDTO bookingDTO);
    ApiResponse<BookingDTO> findBookingByReferenceNo(String  bookingReference);
    //ApiResponse<BookingDTO> updateBooking(BookingDTO bookingDTO);
}
