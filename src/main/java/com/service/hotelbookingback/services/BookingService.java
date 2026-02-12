package com.service.hotelbookingback.services;

import com.service.hotelbookingback.dtos.BookingDTO;
import com.service.hotelbookingback.dtos.ApiResponse;
import com.service.hotelbookingback.entities.Booking;

import java.util.List;

public interface BookingService {
    ApiResponse<List<BookingDTO>> getAllBookings();
    ApiResponse<BookingDTO> createBooking(BookingDTO bookingDTO);
    ApiResponse<BookingDTO> findBookingByReferenceNo(String  bookingReference);
    //ApiResponse<BookingDTO> updateBooking(BookingDTO bookingDTO);
    ApiResponse<BookingDTO> checkIn(String bookingReference);
    ApiResponse<BookingDTO> checkOut(String bookingReference);
}
