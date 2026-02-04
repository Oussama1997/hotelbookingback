package com.service.hotelbookingback.services;

import com.service.hotelbookingback.dtos.BookingDTO;
import com.service.hotelbookingback.dtos.Response;

public interface BookingService {
    Response getAllBookings();
    Response createBooking(BookingDTO bookingDTO);
    Response findBookingByReferenceNo(String  bookingReference);
    Response updateBooking(BookingDTO bookingDTO);
}
