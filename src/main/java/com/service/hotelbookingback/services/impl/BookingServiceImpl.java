package com.service.hotelbookingback.services.impl;

import com.service.hotelbookingback.dtos.BookingDTO;
import com.service.hotelbookingback.dtos.NotificationDTO;
import com.service.hotelbookingback.dtos.Response;
import com.service.hotelbookingback.entities.Booking;
import com.service.hotelbookingback.entities.Room;
import com.service.hotelbookingback.entities.User;
import com.service.hotelbookingback.enums.BookingStatus;
import com.service.hotelbookingback.enums.PaymentStatus;
import com.service.hotelbookingback.exceptions.InvalidBookingStateAndDateException;
import com.service.hotelbookingback.exceptions.NotFoundException;
import com.service.hotelbookingback.services.NotificationService;
import com.service.hotelbookingback.repositories.BookingRepository;
import com.service.hotelbookingback.repositories.RoomRepository;
import com.service.hotelbookingback.services.BookingCodeGenerator;
import com.service.hotelbookingback.services.BookingService;
import com.service.hotelbookingback.services.UserService;
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
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {


    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final BookingCodeGenerator bookingCodeGenerator;


    @Override
    public Response getAllBookings() {
        List<Booking> bookingList =bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<BookingDTO> bookingDTOList = modelMapper.map(bookingList, new TypeToken<List<BookingDTO>>() {}.getType());
        for(BookingDTO bookingDTO: bookingDTOList){
            bookingDTO.setUser(null);
            bookingDTO.setRoom(null);
        }
        return Response.builder()
                .status(200)
                .message("success")
                .bookings(bookingDTOList)
                .build();
    }

    @Override
    public Response createBooking(BookingDTO bookingDTO) {
        User currentUser = userService.getCurrentLoggedInUser();
        Room room = roomRepository.findById(bookingDTO.getRoomId())
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
        //validate room availability
        boolean isAvailable = bookingRepository.isRoomAvailable(room.getId(), bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());
        if (!isAvailable) {
            throw new InvalidBookingStateAndDateException("Room is not available for the selected date ranges");
        }
        BookingDTO savedBooking = saveBooking(bookingDTO, room, currentUser);
        return Response.builder()
                .status(200)
                .message("Booking is successfully")
                .booking(savedBooking)
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
        booking.setTotalPrice(totalPrice);
        booking.setBookingReference(bookingReference);
        booking.setBookingStatus(BookingStatus.BOOKED);
        booking.setPaymentStatus(PaymentStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        Booking savedBooking = bookingRepository.save(booking); //save to database
        sendNotification(currentUser,bookingReference, totalPrice);
        return modelMapper.map(savedBooking, BookingDTO.class);
    }

    private void sendNotification(User currentUser,  String bookingReference, BigDecimal totalPrice){
        //generate the payment url which will be sent via mail
        String paymentUrl = "http://localhost:3000/payment/" + bookingReference + "/" + totalPrice;
        log.info("PAYMENT LINK: {}", paymentUrl);
        //send notification via email
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(currentUser.getEmail())
                .subject("Booking Confirmation")
                .body(String.format("Your booking has been created successfully. Please proceed with your payment using the payment link below " +
                        "\n%s", paymentUrl))
                .bookingReference(bookingReference)
                .build();
        notificationService.sendEmail(notificationDTO);// sending email
    }

    @Override
    public Response findBookingByReferenceNo(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(()-> new NotFoundException("Booking with Reference No: " + bookingReference + " Not found"));
        BookingDTO bookingDTO = modelMapper.map(booking, BookingDTO.class);
        return  Response.builder()
                .status(200)
                .message("success")
                .booking(bookingDTO)
                .build();
    }

    @Override
    public Response updateBooking(BookingDTO bookingDTO) {
        if (bookingDTO.getId() == null) throw new NotFoundException("Booking id is required");
        Booking existingBooking = bookingRepository.findById(bookingDTO.getId())
                .orElseThrow(()-> new NotFoundException("Booking Not Found"));
        if (bookingDTO.getBookingStatus() != null) {
            existingBooking.setBookingStatus(bookingDTO.getBookingStatus());
            // Add payment date column & add refund date
        }
        if (bookingDTO.getPaymentStatus() != null) {
            existingBooking.setPaymentStatus(bookingDTO.getPaymentStatus());
        }
        bookingRepository.save(existingBooking);
        return Response.builder()
                .status(200)
                .message("Booking Updated Successfully")
                .build();
    }


    private BigDecimal calculateTotalPrice(Room room, BookingDTO bookingDTO){
        BigDecimal pricePerNight = room.getPricePerNight();
        long days = ChronoUnit.DAYS.between(bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());
        return pricePerNight.multiply(BigDecimal.valueOf(days));
    }
}
