package com.service.hotelbookingback.configs;

import com.service.hotelbookingback.entities.Booking;
import com.service.hotelbookingback.enums.BookingStatus;
import com.service.hotelbookingback.enums.PaymentStatus;
import com.service.hotelbookingback.repositories.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingExpirationScheduler {

    private final BookingRepository bookingRepository;

    // runs every minute
    @Scheduled(fixedRate = 60000)
    public void cancelExpiredBookings() {

        List<Booking> expiredBookings =
                bookingRepository.findByBookingStatusAndPaymentDeadlineBefore(
                        BookingStatus.PENDING_PAYMENT,
                        LocalDateTime.now()
                );

        for (Booking booking : expiredBookings) {
            log.info("Cancelling expired booking {}", booking.getBookingReference());

            booking.setBookingStatus(BookingStatus.CANCELLED);
            booking.setPaymentStatus(PaymentStatus.EXPIRED);

            bookingRepository.save(booking);
        }
    }

    @Scheduled(cron = "0 0 * * * *") // every hour
    public void markNoShowBookings() {

        List<Booking> noShows = bookingRepository
                .findByBookingStatusAndCheckInDateBefore(
                        BookingStatus.CONFIRMED,
                        LocalDate.now()
                );

        for (Booking booking : noShows) {
            booking.setBookingStatus(BookingStatus.NO_SHOW);
            bookingRepository.save(booking);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void completeFinishedStays() {

        List<Booking> finished = bookingRepository
                .findByBookingStatusAndCheckInDateIsNotNull(BookingStatus.CHECKED_OUT);

        for (Booking booking : finished) {
            booking.setBookingStatus(BookingStatus.COMPLETED);
            bookingRepository.save(booking);
        }
    }
}

