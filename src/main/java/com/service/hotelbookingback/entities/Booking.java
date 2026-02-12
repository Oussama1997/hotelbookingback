package com.service.hotelbookingback.entities;

import com.service.hotelbookingback.enums.BookingStatus;
import com.service.hotelbookingback.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "room_id")
    private Room room;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal totalPrice;
    private String bookingReference;
    private int guests;

    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;

    @Column(name = "payment_deadline")
    private LocalDateTime paymentDeadline;

    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(columnDefinition = "TEXT")
    private String specialRequests;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;
}
