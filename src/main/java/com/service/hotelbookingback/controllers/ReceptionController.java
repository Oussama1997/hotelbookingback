package com.service.hotelbookingback.controllers;

import com.service.hotelbookingback.dtos.ApiResponse;
import com.service.hotelbookingback.dtos.BookingDTO;
import com.service.hotelbookingback.services.ReceptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reception")
@RequiredArgsConstructor
public class ReceptionController {

    private final ReceptionService receptionService;

    @PutMapping("/{ref}/check-in")
    public ResponseEntity<ApiResponse<BookingDTO>> checkIn(@PathVariable String ref) {
        return ResponseEntity.ok(receptionService.checkIn(ref));
    }

    @PutMapping("/{ref}/check-out")
    public ResponseEntity<ApiResponse<BookingDTO>> checkOut(@PathVariable String ref) {
        return ResponseEntity.ok(receptionService.checkOut(ref));
    }

    @GetMapping("/arrivals")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getTodayArrivals() {
        return ResponseEntity.ok(receptionService.getTodayArrivals());

    }

    @GetMapping("/departures")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getTodayDepartures() {
        return ResponseEntity.ok(receptionService.getTodayDepartures());
    }

    @GetMapping("/in-house")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getInHouseGuests() {
        return ResponseEntity.ok(receptionService.getInHouseGuests());
    }
}

