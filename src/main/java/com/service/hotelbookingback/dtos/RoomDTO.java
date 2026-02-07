package com.service.hotelbookingback.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.service.hotelbookingback.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomDTO {

    private Long id;
    private Integer roomNumber;
    private RoomType type;
    private BigDecimal pricePerNight;
    private Integer capacity;
    private String description;
    private boolean isAvailable;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
