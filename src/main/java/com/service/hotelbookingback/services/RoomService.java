package com.service.hotelbookingback.services;

import com.service.hotelbookingback.dtos.Response;
import com.service.hotelbookingback.dtos.RoomDTO;
import com.service.hotelbookingback.enums.RoomType;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomService {
    Response addRoom(RoomDTO roomDTO, MultipartFile imageFile);
    Response updateRoom(RoomDTO roomDTO, MultipartFile imageFile);
    Response getAllRooms();
    Response getRoomById(Long id);
    Response deleteRoom(Long id);
    Response getAvailableRooms(LocalDateTime checkInDate, LocalDateTime checkOutDate, RoomType roomType);
    List<RoomType> getAllRoomTypes();
    Response searchRoom(String input);
}
