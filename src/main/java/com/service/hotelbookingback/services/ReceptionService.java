package com.service.hotelbookingback.services;

import com.service.hotelbookingback.dtos.ApiResponse;
import com.service.hotelbookingback.dtos.BookingDTO;

import java.util.List;

public interface ReceptionService {
    ApiResponse<BookingDTO> checkIn(String bookingReference);
    ApiResponse<BookingDTO> checkOut(String bookingReference);
    ApiResponse<List<BookingDTO>> getTodayArrivals();
    ApiResponse<List<BookingDTO>> getTodayDepartures();
    ApiResponse<List<BookingDTO>> getInHouseGuests();
}
