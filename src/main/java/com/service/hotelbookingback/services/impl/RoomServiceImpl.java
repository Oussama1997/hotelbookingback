package com.service.hotelbookingback.services.impl;

import com.service.hotelbookingback.dtos.ApiResponse;
import com.service.hotelbookingback.dtos.room.RoomDTO;
import com.service.hotelbookingback.dtos.room.RoomRequestDTO;
import com.service.hotelbookingback.dtos.room.SearchRoomRequest;
import com.service.hotelbookingback.entities.Room;
import com.service.hotelbookingback.enums.ImageType;
import com.service.hotelbookingback.enums.RoomType;
import com.service.hotelbookingback.exceptions.InvalidBookingStateAndDateException;
import com.service.hotelbookingback.exceptions.InvalidRequestException;
import com.service.hotelbookingback.exceptions.NotFoundException;
import com.service.hotelbookingback.repositories.RoomRepository;
import com.service.hotelbookingback.services.FileStorageService;
import com.service.hotelbookingback.services.RoomService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final FileStorageService fileStorageService;

    @Override
    public ApiResponse<RoomDTO> createRoom(RoomRequestDTO roomRequest, MultipartFile[] images) {
        Room roomToSave = modelMapper.map(roomRequest, Room.class);
        if (images != null && images.length > 0) {
            List<String> savedFileNames = fileStorageService.storeRoomImages(images);
            roomToSave.setImageFileNames(savedFileNames);
        }
        roomRepository.save(roomToSave);
        return ApiResponse.<RoomDTO>builder()
                .status(200)
                .message("Room successfully added")
                .build();
    }

    @Override
    public ApiResponse<RoomDTO> updateRoom(String roomNumber, RoomRequestDTO roomRequest, MultipartFile[] newImages, List<String> imagesToDelete) {
        Room existingRoom = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(()-> new NotFoundException("Room not found"));
        if (roomRequest.getRoomNumber() != null){
            existingRoom.setRoomNumber(roomRequest.getRoomNumber());
        }
        if (roomRequest.getPricePerNight() != null && roomRequest.getPricePerNight().compareTo(BigDecimal.ZERO) > 0){
            existingRoom.setPricePerNight(roomRequest.getPricePerNight());
        }
        if (roomRequest.getCapacity() > 0){
            existingRoom.setCapacity(roomRequest.getCapacity());
        }
        if (roomRequest.getType() != null) existingRoom.setType(roomRequest.getType());
        if(roomRequest.getDescription() != null) existingRoom.setDescription(roomRequest.getDescription());
        if (imagesToDelete != null && !imagesToDelete.isEmpty()) {
            List<String> currentImages = existingRoom.getImageFileNames();
            List<String> imagesToKeep = currentImages.stream()
                    .filter(image -> !imagesToDelete.contains(image))
                    .collect(Collectors.toList());
            // Delete files from storage
            fileStorageService.deleteFiles(imagesToDelete, ImageType.ROOM_IMAGE);
            existingRoom.setImageFileNames(imagesToKeep);
        }
        // Add new images
        if (newImages != null && newImages.length > 0) {
            List<String> newFileNames = fileStorageService.storeRoomImages(newImages);
            existingRoom.getImageFileNames().addAll(newFileNames);
        }
        roomRepository.save(existingRoom);
        return ApiResponse.<RoomDTO>builder()
                .status(200)
                .message("Room updated successfully")
                .build();
    }

    @Override
    public ApiResponse<RoomDTO> addImagesToRoom(Long id, MultipartFile[] images) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<String> newFileNames = fileStorageService.storeRoomImages(images);
        room.getImageFileNames().addAll(newFileNames);

        roomRepository.save(room);
        return ApiResponse.<RoomDTO>builder()
                .status(200)
                .message("Added Successfully")
                .build();
    }

    @Override
    public ApiResponse<RoomDTO> deleteImagesFromRoom(Long id, List<String> imageFileNames) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<String> currentImages = room.getImageFileNames();
        List<String> imagesToKeep = currentImages.stream()
                .filter(image -> !imageFileNames.contains(image))
                .collect(Collectors.toList());

        // Delete files from storage
        fileStorageService.deleteFiles(imageFileNames, ImageType.ROOM_IMAGE);

        room.setImageFileNames(imagesToKeep);
        roomRepository.save(room);
        return ApiResponse.<RoomDTO>builder()
                .status(200)
                .message("Deleted Successfully")
                .build();
    }

    @Override
    public ApiResponse<List<RoomDTO>> getAllRooms() {
        List<RoomDTO> roomDTOList = roomRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream().map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ApiResponse.<List<RoomDTO>>builder()
                .status(200)
                .message("success")
                .data(roomDTOList)
                .build();
    }

    @Override
    public ApiResponse<RoomDTO> getRoomByNumber(String roomNumber) {
        Room room = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(()-> new NotFoundException("Room not found"));
        return ApiResponse.<RoomDTO>builder()
                .status(200)
                .message("success")
                .data(convertToResponseDTO(room))
                .build();
    }

    @Override
    public ApiResponse deleteRoom(String roomNumber) {
        Room room = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        // Delete all associated images
        if (room.getImageFileNames() != null && !room.getImageFileNames().isEmpty()) {
            fileStorageService.deleteFiles(room.getImageFileNames(), ImageType.ROOM_IMAGE);
        }
        roomRepository.delete(room);
        return ApiResponse.builder()
                .status(200)
                .message("Room Deleted Successfully")
                .build();
    }

    @Override
    public ApiResponse<List<RoomDTO>> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate) {
        //validation: Ensure the check-in date is not before today
        if (checkInDate.isBefore(LocalDate.now())){
            throw new InvalidBookingStateAndDateException("check in date cannot be before today ");
        }
        //validation: Ensure the check-out date is not before check in date
        if (checkOutDate.isBefore(checkInDate)){
            throw new InvalidBookingStateAndDateException("check out date cannot be before check in date ");
        }
        //validation: Ensure the check-in date is not same as check out date
        if (checkInDate.isEqual(checkOutDate)){
            throw new InvalidBookingStateAndDateException("check in date cannot be equal to check out date ");
        }
        List<RoomDTO> roomDTOList = roomRepository.findAvailableRooms(checkInDate, checkOutDate)
                .stream().map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ApiResponse.<List<RoomDTO>>builder()
                .status(200)
                .message("success")
                .data(roomDTOList)
                .build();
    }

    @Override
    public ApiResponse<List<RoomType>> getAllRoomTypes() {
        return ApiResponse.<List<RoomType>>builder()
                .status(200)
                .message("success")
                .data(Arrays.asList(RoomType.values()))
                .build();
    }

    @Override
    public ApiResponse<List<RoomDTO>> searchRooms(String input) {
        List<RoomDTO> roomDTOList = roomRepository.searchRooms(input)
                .stream().map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ApiResponse.<List<RoomDTO>>builder()
                .status(200)
                .message("success")
                .data(roomDTOList)
                .build();
    }

    @Override
    public ApiResponse<List<RoomDTO>> searchRooms(SearchRoomRequest request) {
        // Validate request is not null
        if (request == null) {
            throw new InvalidRequestException("Search request cannot be null");
        }
        // Validate dates are not null
        if (request.getCheckInDate() == null || request.getCheckOutDate() == null) {
            throw new InvalidRequestException("Check-in and check-out dates must be specified");
        }
        //validation: Ensure the check-in date is not before today
        if (request.getCheckInDate().isBefore(LocalDate.now())){
            throw new InvalidBookingStateAndDateException("check in date cannot be before today ");
        }
        //validation: Ensure the check-out date is not before check in date
        if (request.getCheckOutDate().isBefore(request.getCheckInDate())){
            throw new InvalidBookingStateAndDateException("check out date cannot be before check in date ");
        }
        //validation: Ensure the check-in date is not same as check out date
        if (request.getCheckInDate().isEqual(request.getCheckOutDate())){
            throw new InvalidBookingStateAndDateException("check in date cannot be equal to check out date ");
        }
        List<RoomDTO> filteredRooms;
        if(request.getRoomType() == null){
            filteredRooms = roomRepository
                    .findAvailableRooms(request.getCheckInDate(),request.getCheckOutDate())
                    .stream().map(this::convertToResponseDTO)
                    .collect(Collectors.toList());

        } else {
            // Apply filters only when criteria are specified
            filteredRooms = roomRepository
                    .findAvailableRooms(request.getCheckInDate(),request.getCheckOutDate())
                    .stream()
                    .filter(room -> request.getGuests() == 0 || room.getCapacity() >= request.getGuests())
                    .filter(room -> request.getRoomType() == null || request.getRoomType().equals(room.getType()))
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());
        }
        return ApiResponse.<List<RoomDTO>>builder()
                .status(200)
                .message("success")
                .data(filteredRooms)
                .build();
    }

    private RoomDTO convertToResponseDTO(Room room) {
        RoomDTO dto = modelMapper.map(room, RoomDTO.class);
        dto.setImageUrls(room.getImageUrls());
        dto.setPrimaryImageUrl(room.getPrimaryImageUrl());
        return dto;
    }
}