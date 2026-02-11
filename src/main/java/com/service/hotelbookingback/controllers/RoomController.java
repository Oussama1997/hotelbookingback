package com.service.hotelbookingback.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.service.hotelbookingback.dtos.ApiResponse;
import com.service.hotelbookingback.dtos.room.RoomDTO;
import com.service.hotelbookingback.dtos.room.RoomRequestDTO;
import com.service.hotelbookingback.dtos.room.SearchRoomRequest;
import com.service.hotelbookingback.enums.RoomType;
import com.service.hotelbookingback.services.RoomService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final ModelMapper modelMapper;

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<RoomDTO>> createRoom(
            @RequestPart("room") String roomJson,
            @RequestPart(value = "images", required = false) MultipartFile[] images) throws JsonProcessingException {
        RoomRequestDTO roomRequest = modelMapper.map(roomJson,new TypeToken<RoomRequestDTO>() {}.getType());
        return ResponseEntity.ok(roomService.createRoom(roomRequest, images));
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<RoomDTO>> updateRoom(
            @PathVariable Long id,
            @RequestPart("room") String roomJson,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "imagesToDelete", required = false) List<String> imagesToDelete) throws JsonProcessingException {
        RoomRequestDTO roomRequest = modelMapper.map(roomJson,new TypeToken<RoomRequestDTO>() {}.getType());
        return ResponseEntity.ok(roomService.updateRoom(id, roomRequest, images, imagesToDelete));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<RoomDTO>>> getAllRooms(){
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomDTO>> getRoomById(@PathVariable Long id){
        return ResponseEntity.ok(roomService.getRoomById(id));
    }


    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id){
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/availables")
    public ResponseEntity<ApiResponse<List<RoomDTO>>> getAvailableRooms(
            @RequestParam LocalDate checkInDate,
            @RequestParam LocalDate checkOutDate
    ){
        return ResponseEntity.ok(roomService.getAvailableRooms(checkInDate, checkOutDate));
    }

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<RoomType>>> getAllRoomTypes(){
        return ResponseEntity.ok(roomService.getAllRoomTypes());
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<RoomDTO>>> searchRoom(@RequestBody SearchRoomRequest request){
        return ResponseEntity.ok(roomService.searchRooms(request));
    }

    // Add images to existing room
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> addRoomImages(
            @PathVariable Long id,
            @RequestPart("images") MultipartFile[] images) {
        return ResponseEntity.ok(roomService.addImagesToRoom(id, images));
    }

    // Delete specific images from room
    @DeleteMapping("/{id}/images")
    public ResponseEntity<ApiResponse> deleteRoomImages(
            @PathVariable Long id,
            @RequestParam List<String> imageFileNames) {
        return ResponseEntity.ok(roomService.deleteImagesFromRoom(id, imageFileNames));
    }
}