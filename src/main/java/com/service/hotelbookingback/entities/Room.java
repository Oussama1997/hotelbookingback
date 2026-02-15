package com.service.hotelbookingback.entities;

import com.service.hotelbookingback.enums.RoomType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "rooms")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Room Number is required")
    @Column(unique = true)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Room type is required")
    @Column(nullable = false)
    private RoomType type;

    @DecimalMin(value = "0.1", message = "Price per night is required")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Min(value = 1, message = "Capacity must be at least 1")
    @Column(nullable = false)
    private Integer capacity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "room_amenities", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "amenity")
    private List<String> amenities = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "room_images", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "image_file_name")
    private List<String> imageFileNames = new ArrayList<>();

    @Transient
    public List<String> getImageUrls() {
        return imageFileNames.stream()
                .map(fileName -> "/api/images/rooms/" + fileName)
                .collect(Collectors.toList());
    }

    @Transient
    public String getPrimaryImageUrl() {
        return !imageFileNames.isEmpty() ?
                "/api/images/rooms/" + imageFileNames.get(0) :
                "/api/images/rooms/default-room.jpg";
    }

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;
}
