package com.service.hotelbookingback.repositories;

import com.service.hotelbookingback.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("""
            SELECT r FROM Room r
            WHERE
                r.id NOT IN (
                    SELECT b.room.id
                    FROM Booking b
                    WHERE :checkInDate <= b.checkOutDate
                    AND :checkOutDate >= b.checkInDate
                    AND b.status IN ('BOOKED', 'CHECKED_IN')
                )
            """)
    List<Room> findAvailableRooms(
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

    @Query("""
            SELECT r FROM Room r
                WHERE CAST(r.roomNumber AS string) LIKE %:searchParam%
                   OR LOWER(r.type) LIKE LOWER(:searchParam)
                   OR CAST(r.pricePerNight AS string) LIKE %:searchParam%
                   OR CAST(r.capacity AS string) LIKE %:searchParam%
                   OR LOWER(r.description) LIKE LOWER(CONCAT('%', :searchParam, '%'))
            """)
    List<Room> searchRooms(@Param("searchParam") String searchParam);

    Optional<Room> findByRoomNumber(String roomNumber);
}
