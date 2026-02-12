package com.service.hotelbookingback.dtos.room;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.service.hotelbookingback.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomDTO {

    private String roomNumber;
    private RoomType type;
    private BigDecimal pricePerNight;
    private Integer capacity;
    private String description;
    private List<String> imageUrls;
    private String primaryImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
