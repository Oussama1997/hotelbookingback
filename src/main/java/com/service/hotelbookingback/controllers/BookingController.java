package com.service.hotelbookingback.controllers;

import com.service.hotelbookingback.dtos.BookingDTO;
import com.service.hotelbookingback.dtos.ApiResponse;
import com.service.hotelbookingback.services.interfaces.BookingService;
import com.service.hotelbookingback.services.interfaces.UserService;
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

    @PutMapping("/refund/{reference}")
    public ResponseEntity<?> refundBooking(@PathVariable String reference) {
        if(bookingService.cancelBooking(reference)){
            return ResponseEntity.badRequest()
                    .body("Booking cannot be cancelled less than 24h before check-in");
        }
        return ResponseEntity.ok("Booking cancelled successfully");
    }
}