package com.service.hotelbookingback.services;

import com.service.hotelbookingback.dtos.ApiResponse;
import com.service.hotelbookingback.dtos.RoomDTO;
import com.service.hotelbookingback.dtos.SearchRoomRequest;
import com.service.hotelbookingback.enums.RoomType;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface RoomService {
    ApiResponse addRoom(RoomDTO roomDTO, MultipartFile imageFile);
    ApiResponse updateRoom(RoomDTO roomDTO, MultipartFile imageFile);
    ApiResponse<List<RoomDTO>> getAllRooms();
    ApiResponse<RoomDTO> getRoomById(Long id);
    ApiResponse deleteRoom(Long id);
    ApiResponse<List<RoomDTO>> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate);
    ApiResponse<List<RoomType>> getAllRoomTypes();
    ApiResponse<List<RoomDTO>> searchRooms(String searchParam);
    ApiResponse<List<RoomDTO>> searchRoom(SearchRoomRequest request);
}
