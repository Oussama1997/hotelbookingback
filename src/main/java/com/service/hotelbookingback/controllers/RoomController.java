package com.service.hotelbookingback.controllers;

import com.service.hotelbookingback.dtos.ApiResponse;
import com.service.hotelbookingback.dtos.RoomDTO;
import com.service.hotelbookingback.enums.RoomType;
import com.service.hotelbookingback.services.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse> addRoom(
            @RequestParam Integer roomNumber,
            @RequestParam RoomType type,
            @RequestParam BigDecimal pricePerNight,
            @RequestParam Integer capacity,
            @RequestParam String  description,
            @RequestParam MultipartFile imageFile
    ){
        RoomDTO roomDTO =RoomDTO.builder()
                .roomNumber(roomNumber)
                .type(type)
                .pricePerNight(pricePerNight)
                .capacity(capacity)
                .description(description)
                .build();
        return  ResponseEntity.ok(roomService.addRoom(roomDTO, imageFile));
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse> updateRoom(
            @RequestParam (value = "roomNumber", required = false) Integer roomNumber,
            @RequestParam (value = "type", required = false) RoomType type,
            @RequestParam (value = "pricePerNight", required = false) BigDecimal pricePerNight,
            @RequestParam (value = "capacity", required = false) Integer capacity,
            @RequestParam (value = "description", required = false) String  description,
            @RequestParam (value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam (value = "id", required = true) Long id
    ){
        RoomDTO roomDTO =RoomDTO.builder()
                .id(id)
                .roomNumber(roomNumber)
                .type(type)
                .pricePerNight(pricePerNight)
                .capacity(capacity)
                .description(description)
                .build();
        return  ResponseEntity.ok(roomService.updateRoom(roomDTO, imageFile));

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
    public ResponseEntity<ApiResponse> deleteRoom(@PathVariable Long id){
        return ResponseEntity.ok(roomService.deleteRoom(id));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<RoomDTO>>> getAvailableRooms(
            @RequestParam LocalDateTime checkInDate,
            @RequestParam LocalDateTime checkOutDate,
            @RequestParam(required = false) RoomType roomType
    ){
        return ResponseEntity.ok(roomService.getAvailableRooms(checkInDate, checkOutDate, roomType));
    }

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<RoomType>>> getAllRoomTypes(){
        return ResponseEntity.ok(roomService.getAllRoomTypes());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<RoomDTO>>> searchRoom(@RequestParam String input){
        return ResponseEntity.ok(roomService.searchRoom(input));
    }
}