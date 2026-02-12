package com.service.hotelbookingback.controllers;

import com.service.hotelbookingback.dtos.BookingDTO;
import com.service.hotelbookingback.dtos.ApiResponse;
import com.service.hotelbookingback.services.BookingService;
import com.service.hotelbookingback.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getAllBookings(){
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<ApiResponse<BookingDTO>> createBooking(@RequestBody BookingDTO bookingDTO){
        return ResponseEntity.ok(bookingService.createBooking(bookingDTO));
    }

    @GetMapping("/{reference}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<ApiResponse<BookingDTO>> findBookingByReferenceNo(@PathVariable String reference){
        return ResponseEntity.ok(bookingService.findBookingByReferenceNo(reference));
    }

    /*@PutMapping("/update")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<ApiResponse<BookingDTO>> updateBooking(@RequestBody BookingDTO bookingDTO){
        return ResponseEntity.ok(bookingService.updateBooking(bookingDTO));
    }*/

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @GetMapping("/account/all")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getMyBookingHistory(){
        return ResponseEntity.ok(userService.getMyBookingHistory());
    }

    @PutMapping("/{ref}/check-in")
    public ResponseEntity<ApiResponse<BookingDTO>> checkIn(@PathVariable String ref) {
        return ResponseEntity.ok(bookingService.checkIn(ref));
    }

    @PutMapping("/{ref}/check-out")
    public ResponseEntity<ApiResponse<BookingDTO>> checkOut(@PathVariable String ref) {
        return ResponseEntity.ok(bookingService.checkOut(ref));
    }
}