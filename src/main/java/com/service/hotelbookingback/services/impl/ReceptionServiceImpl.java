package com.service.hotelbookingback.services.impl;

import com.service.hotelbookingback.dtos.ApiResponse;
import com.service.hotelbookingback.dtos.BookingDTO;
import com.service.hotelbookingback.entities.Booking;
import com.service.hotelbookingback.enums.BookingStatus;
import com.service.hotelbookingback.exceptions.NotFoundException;
import com.service.hotelbookingback.repositories.BookingRepository;
import com.service.hotelbookingback.services.interfaces.ReceptionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ReceptionServiceImpl implements ReceptionService {

    private final BookingRepository bookingRepository;
    private final ModelMapper modelMapper;


    @Override
    public ApiResponse<List<BookingDTO>> getTodayArrivals() {
        List<BookingDTO> bookingList = bookingRepository.findByCheckInDateAndStatus(
                LocalDate.now(),
                BookingStatus.CONFIRMED)
                .stream().map(this::convertToResponseDTO)
                .toList();
        return ApiResponse.<List<BookingDTO>>builder()
                .status(200)
                .data(bookingList)
                .message("Room successfully added")
                .build();
    }

    @Override
    public ApiResponse<List<BookingDTO>> getTodayDepartures() {
        List<BookingDTO> bookingList = bookingRepository.findByCheckOutDateAndStatus(
                LocalDate.now(),
                BookingStatus.CHECKED_IN)
                .stream().map(this::convertToResponseDTO)
                .toList();
        return ApiResponse.<List<BookingDTO>>builder()
                .status(200)
                .data(bookingList)
                .message("Room successfully added")
                .build();
    }

    @Override
    public ApiResponse<List<BookingDTO>> getInHouseGuests() {
        List<BookingDTO> bookingList = bookingRepository.findByStatus(
                BookingStatus.CHECKED_IN)
                .stream().map(this::convertToResponseDTO)
                .toList();
        return ApiResponse.<List<BookingDTO>>builder()
                .status(200)
                .data(bookingList)
                .message("Room successfully added")
                .build();
    }

    @Transactional
    public ApiResponse<BookingDTO> checkIn(String bookingReference) {

        Booking booking = bookingRepository.findByReference(bookingReference)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (booking.getCheckInDate() != null) {
            throw new RuntimeException("Guest already checked in");
        }
        if (LocalDate.now().isBefore(booking.getCheckInDate())) {
            throw new RuntimeException("Guest cannot check-in before arrival date");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Booking is not ready for check-in");
        }

        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setCheckInDate(LocalDate.now());

        Booking savedB = bookingRepository.save(booking);
        BookingDTO bookingDTO = modelMapper.map(savedB, BookingDTO.class);
        return  ApiResponse.<BookingDTO>builder()
                .status(200)
                .message("success")
                .data(bookingDTO)
                .build();
    }

    @Transactional
    public ApiResponse<BookingDTO> checkOut(String bookingReference) {

        Booking booking = bookingRepository.findByReference(bookingReference)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (booking.getCheckInDate() == null) {
            throw new RuntimeException("Guest never checked in");
        }
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new RuntimeException("Guest is not checked-in");
        }
        if (booking.getCheckOutDate() != null) {
            throw new RuntimeException("Guest already checked out");
        }

        booking.setStatus(BookingStatus.CHECKED_OUT);
        booking.setCheckOutDate(LocalDate.now());

        Booking savedB = bookingRepository.save(booking);
        BookingDTO bookingDTO = modelMapper.map(savedB, BookingDTO.class);
        return  ApiResponse.<BookingDTO>builder()
                .status(200)
                .message("success")
                .data(bookingDTO)
                .build();
    }

    private BookingDTO convertToResponseDTO(Booking booking) {
        return modelMapper.map(booking, BookingDTO.class);
    }
}
