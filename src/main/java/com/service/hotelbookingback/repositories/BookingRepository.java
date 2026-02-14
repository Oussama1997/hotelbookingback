package com.service.hotelbookingback.repositories;

import com.service.hotelbookingback.entities.Booking;
import com.service.hotelbookingback.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);
    Optional<Booking> findByReference(String reference);
    boolean existsByUserIdAndStatus(Long userId, BookingStatus status);
    List<Booking> findByStatusAndPaymentDeadlineBefore(
            BookingStatus status, LocalDateTime time);
    List<Booking> findByStatusAndCheckInDateBefore(
            BookingStatus status, LocalDate date);
    List<Booking> findByStatusAndCheckInDateIsNotNull(BookingStatus status);
    List<Booking> findByCheckInDateAndStatus(
            LocalDate date,
            BookingStatus status);
    List<Booking> findByCheckOutDateAndStatus(
            LocalDate date,
            BookingStatus status);
    List<Booking> findByStatus(BookingStatus status);
    @Query("""
               SELECT CASE WHEN COUNT(b) = 0 THEN true ELSE false END
                FROM Booking b
                WHERE b.room.id = :roomId
                  AND :checkInDate <= b.checkOutDate
                  AND :checkOutDate >= b.checkInDate
                  AND b.status IN ('PENDING_PAYMENT','CONFIRMED','CHECKED_IN')
            """)
    boolean isRoomAvailable(@Param("roomId") Long roomId,
                            @Param("checkInDate") LocalDate checkInDate,
                            @Param("checkOutDate") LocalDate checkOutDate);
}