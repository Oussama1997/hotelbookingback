package com.service.hotelbookingback.utils;

import com.service.hotelbookingback.dtos.BookingDTO;
import com.service.hotelbookingback.dtos.UserDTO;
import com.service.hotelbookingback.dtos.payment.PaymentDTO;
import com.service.hotelbookingback.dtos.room.RoomDTO;
import com.service.hotelbookingback.dtos.room.RoomRequestDTO;
import com.service.hotelbookingback.entities.Booking;
import com.service.hotelbookingback.entities.Payment;
import com.service.hotelbookingback.entities.Room;
import com.service.hotelbookingback.entities.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public final class Converter {

    private static ModelMapper modelMapper;

    public static UserDTO convertToResponseDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    public static RoomDTO convertToResponseDTO(Room room) {
        RoomDTO dto = modelMapper.map(room, RoomDTO.class);
        dto.setImageUrls(room.getImageUrls());
        dto.setPrimaryImageUrl(room.getPrimaryImageUrl());
        return dto;
    }

    public static Room convertToEntity(RoomRequestDTO roomRequest) {
        return modelMapper.map(roomRequest, Room.class);
    }

    public static BookingDTO convertToResponseDTO(Booking booking) {
        return modelMapper.map(booking, BookingDTO.class);
    }

    public static PaymentDTO convertToResponseDTO(Payment payment) {
        return modelMapper.map(payment, PaymentDTO.class);
    }
}
