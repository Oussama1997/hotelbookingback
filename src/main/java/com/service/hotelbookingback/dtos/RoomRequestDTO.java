package com.service.hotelbookingback.dtos;

import com.service.hotelbookingback.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomRequestDTO {
    private String roomNumber;
    private RoomType type;
    private BigDecimal pricePerNight;
    private String description;
    private int capacity;
}
