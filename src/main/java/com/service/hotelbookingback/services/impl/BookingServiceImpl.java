package com.service.hotelbookingback.services.impl;

import com.service.hotelbookingback.configs.EmailTemplateConfig;
import com.service.hotelbookingback.dtos.BookingDTO;
import com.service.hotelbookingback.dtos.NotificationDTO;
import com.service.hotelbookingback.dtos.ApiResponse;
import com.service.hotelbookingback.dtos.email.EmailNotificationRequest;
import com.service.hotelbookingback.entities.Booking;
import com.service.hotelbookingback.entities.Room;
import com.service.hotelbookingback.entities.User;
import com.service.hotelbookingback.enums.BookingStatus;
import com.service.hotelbookingback.enums.EmailTemplate;
import com.service.hotelbookingback.enums.PaymentStatus;
import com.service.hotelbookingback.exceptions.InvalidBookingStateAndDateException;
import com.service.hotelbookingback.exceptions.NotFoundException;
import com.service.hotelbookingback.services.EmailService;
import com.service.hotelbookingback.services.NotificationService;
import com.service.hotelbookingback.repositories.BookingRepository;
import com.service.hotelbookingback.repositories.RoomRepository;
import com.service.hotelbookingback.services.BookingService;
import com.service.hotelbookingback.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final int PAYMENT_EXPIRATION_MINUTES = 15;

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final BookingCodeGenerator bookingCodeGenerator;
    private final EmailTemplateConfig emailTemplateConfig;
    private final EmailService emailService;

    @Override
    public ApiResponse<List<BookingDTO>> getAllBookings() {
        List<Booking> bookingList =bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<BookingDTO> bookingDTOList = modelMapper.map(bookingList, new TypeToken<List<BookingDTO>>() {}.getType());
        for(BookingDTO bookingDTO: bookingDTOList){
            bookingDTO.setUser(null);
            bookingDTO.setRoom(null);
        }
        return ApiResponse.<List<BookingDTO>>builder()
                .status(200)
                .message("success")
                .data(bookingDTOList)
                .build();
    }

    @Override
    public ApiResponse<BookingDTO> createBooking(BookingDTO bookingDTO) {
        User currentUser = userService.getCurrentLoggedInUser();
        Room room = roomRepository.findByRoomNumber(bookingDTO.getRoomNumber())
                .orElseThrow(()-> new NotFoundException("Room Not Found"));
        //validation: Ensure the check-in date is not before today
        if (bookingDTO.getCheckInDate().isBefore(LocalDate.now())){
            throw new InvalidBookingStateAndDateException("Check-In date cannot be before today ");
        }
        //validation: Ensure the check-out date is not before check in date
        if (bookingDTO.getCheckOutDate().isBefore(bookingDTO.getCheckInDate())){
            throw new InvalidBookingStateAndDateException("Check-Out date cannot be before Check-In date ");
        }
        //validation: Ensure the check-in date is not same as check out date
        if (bookingDTO.getCheckInDate().isEqual(bookingDTO.getCheckOutDate())){
            throw new InvalidBookingStateAndDateException("Check-In date cannot be equal to Check-Out date ");
        }
        //validation: Prevent booking too far in the past or unrealistic future
        if (bookingDTO.getCheckInDate().isAfter(LocalDate.now().plusYears(2))) {
            throw new InvalidBookingStateAndDateException("Booking too far in future");
        }
        //validation: Prevent multiple pending bookings per user for same dates
        boolean alreadyHasPending = bookingRepository
                .existsByUserIdAndBookingStatus(currentUser.getId(), BookingStatus.PENDING_PAYMENT);
        if (alreadyHasPending) {
            throw new InvalidBookingStateAndDateException(
                    "You already have a pending booking. Please complete payment first."
            );
        }
        //validation: room availability
        boolean isAvailable = bookingRepository.isRoomAvailable(room.getId(), bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());
        if (!isAvailable) {
            throw new InvalidBookingStateAndDateException("Room is not available for the selected date ranges");
        }
        if (bookingDTO.getGuests() > room.getCapacity()) {
            throw new InvalidBookingStateAndDateException("Room capacity exceeded");
        }
        BookingDTO savedBooking = saveBooking(bookingDTO, room, currentUser);
        savedBooking.setRoom(null);
        savedBooking.setUser(null);
        return ApiResponse.<BookingDTO>builder()
                .status(200)
                .message("Booking is successfully")
                .data(savedBooking)
                .build();

    }

    private BookingDTO saveBooking(BookingDTO bookingDTO, Room room, User currentUser){
        //calculate the total price needed to pay for the stay
        BigDecimal totalPrice = calculateTotalPrice(room, bookingDTO);
        String bookingReference = bookingCodeGenerator.generateBookingReference();
        //create and save the booking
        Booking booking = new Booking();
        booking.setUser(currentUser);
        booking.setRoom(room);
        booking.setCheckInDate(bookingDTO.getCheckInDate());
        booking.setCheckOutDate(bookingDTO.getCheckOutDate());
        booking.setGuests(bookingDTO.getGuests());
        booking.setTotalPrice(totalPrice);
        booking.setBookingReference(bookingReference);

        booking.setBookingStatus(BookingStatus.PENDING_PAYMENT);
        booking.setPaymentStatus(PaymentStatus.PENDING);
        booking.setPaymentDeadline(LocalDateTime.now().plusMinutes(PAYMENT_EXPIRATION_MINUTES));

        if (booking.getSpecialRequests() != null){
            booking.setSpecialRequests(bookingDTO.getSpecialRequests());
        }
        Booking savedBooking = bookingRepository.save(booking);
        sendNotifBookCre(currentUser,savedBooking);
        return modelMapper.map(savedBooking, BookingDTO.class);
    }

    private void sendNotifBookCre(User currentUser, Booking booking){
        //generate the payment url which will be sent via mail
        String paymentUrl = "http://localhost:4200/booking/payment/" + booking.getBookingReference();
        log.info("PAYMENT LINK: {}", paymentUrl);
        //send notification via email
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", currentUser.getLastName() + " " + currentUser.getFirstName());
        vars.put("bookingRef", booking.getBookingReference());
        vars.put("room", booking.getRoom().getRoomNumber());
        vars.put("checkIn", booking.getCheckInDate());
        vars.put("checkOut", booking.getCheckOutDate());
        vars.put("amount", booking.getTotalPrice());
        vars.put("paymentUrl", paymentUrl);
        emailService.sendTemplateEmail(currentUser.getEmail(),
                EmailTemplate.BOOKING_CREATION, vars);// sending email
    }

    @Override
    public ApiResponse<BookingDTO> findBookingByReferenceNo(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(()-> new NotFoundException("Booking with Reference No: " + bookingReference + " Not found"));
        if (booking.getPaymentDeadline() != null &&
                booking.getPaymentDeadline().isBefore(LocalDateTime.now()) &&
                booking.getPaymentStatus() == PaymentStatus.PENDING) {

            booking.setBookingStatus(BookingStatus.CANCELLED);
            booking.setPaymentStatus(PaymentStatus.EXPIRED);
            bookingRepository.save(booking);

            throw new RuntimeException("Payment time expired for this booking");
        }
        BookingDTO bookingDTO = modelMapper.map(booking, BookingDTO.class);
        return  ApiResponse.<BookingDTO>builder()
                .status(200)
                .message("success")
                .data(bookingDTO)
                .build();
    }

    /*@Override
    public ApiResponse<BookingDTO> updateBooking(BookingDTO bookingDTO) {
        if (bookingDTO.getId() == null) throw new NotFoundException("Booking id is required");
        Booking existingBooking = bookingRepository.findById(bookingDTO.getId())
                .orElseThrow(()-> new NotFoundException("Booking Not Found"));
        if (bookingDTO.getBookingStatus() != null) {
            existingBooking.setBookingStatus(bookingDTO.getBookingStatus());
            // Add payment date column & add refund date
        }
        if(bookingDTO.getSpecialRequests() != null){
            existingBooking.setSpecialRequests(bookingDTO.getSpecialRequests());
        }
        bookingRepository.save(existingBooking);
        return ApiResponse.<BookingDTO>builder()
                .status(200)
                .message("Booking Updated Successfully")
                .build();
    }*/


    private BigDecimal calculateTotalPrice(Room room, BookingDTO bookingDTO){
        BigDecimal pricePerNight = room.getPricePerNight();
        long days = ChronoUnit.DAYS.between(bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());
        if (days <= 0) {
            throw new InvalidBookingStateAndDateException("Invalid stay duration");
        }
        return pricePerNight.multiply(BigDecimal.valueOf(days)).multiply(BigDecimal.valueOf(1.1)); // add fees and tax
    }

    @Transactional
    public ApiResponse<BookingDTO> checkIn(String bookingReference) {

        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (booking.getCheckInDate() != null) {
            throw new RuntimeException("Guest already checked in");
        }
        if (LocalDate.now().isBefore(booking.getCheckInDate())) {
            throw new RuntimeException("Guest cannot check-in before arrival date");
        }
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Booking is not ready for check-in");
        }

        booking.setBookingStatus(BookingStatus.CHECKED_IN);
        booking.setCheckInDate(LocalDate.now());

        Booking savedB = bookingRepository.save(booking);
        BookingDTO bookingDTO = modelMapper.map(savedB, BookingDTO.class);
        return  ApiResponse.<BookingDTO>builder()
                .status(200)
                .message("success")
                .data(bookingDTO)
                .build();
    }

    @Transactional
    public ApiResponse<BookingDTO> checkOut(String bookingReference) {

        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (booking.getCheckInDate() == null) {
            throw new RuntimeException("Guest never checked in");
        }
        if (booking.getBookingStatus() != BookingStatus.CHECKED_IN) {
            throw new RuntimeException("Guest is not checked-in");
        }
        if (booking.getCheckOutDate() != null) {
            throw new RuntimeException("Guest already checked out");
        }

        booking.setBookingStatus(BookingStatus.CHECKED_OUT);
        booking.setCheckOutDate(LocalDate.now());

        Booking savedB = bookingRepository.save(booking);
        BookingDTO bookingDTO = modelMapper.map(savedB, BookingDTO.class);
        return  ApiResponse.<BookingDTO>builder()
                .status(200)
                .message("success")
                .data(bookingDTO)
                .build();
    }

}
