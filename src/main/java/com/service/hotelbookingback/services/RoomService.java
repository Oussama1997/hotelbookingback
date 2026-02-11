package com.service.hotelbookingback.services;

import com.service.hotelbookingback.dtos.ApiResponse;
import com.service.hotelbookingback.dtos.RoomDTO;
import com.service.hotelbookingback.dtos.RoomRequestDTO;
import com.service.hotelbookingback.dtos.SearchRoomRequest;
import com.service.hotelbookingback.enums.RoomType;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface RoomService {
    ApiResponse<RoomDTO> createRoom(RoomRequestDTO roomRequest, MultipartFile[] images);
    ApiResponse<RoomDTO> updateRoom(Long id, RoomRequestDTO roomRequest, MultipartFile[] newImages, List<String> imagesToDelete);
    ApiResponse<RoomDTO> addImagesToRoom(Long id, MultipartFile[] images);
    ApiResponse<RoomDTO> deleteImagesFromRoom(Long id, List<String> imageFileNames);
    ApiResponse<List<RoomDTO>> getAllRooms();
    ApiResponse<RoomDTO> getRoomById(Long id);
    ApiResponse<RoomDTO> deleteRoom(Long id);
    ApiResponse<List<RoomDTO>> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate);
    ApiResponse<List<RoomType>> getAllRoomTypes();
    ApiResponse<List<RoomDTO>> searchRooms(String searchParam);
    ApiResponse<List<RoomDTO>> searchRooms(SearchRoomRequest request);
}
