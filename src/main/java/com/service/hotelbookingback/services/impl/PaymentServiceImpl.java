package com.service.hotelbookingback.services.impl;


import com.service.hotelbookingback.dtos.NotificationDTO;
import com.service.hotelbookingback.entities.Booking;
import com.service.hotelbookingback.entities.Payment;
import com.service.hotelbookingback.enums.NotificationType;
import com.service.hotelbookingback.enums.PaymentGateway;
import com.service.hotelbookingback.exceptions.NotFoundException;
import com.service.hotelbookingback.dtos.payment.PaymentRequest;
import com.service.hotelbookingback.repositories.BookingRepository;
import com.service.hotelbookingback.repositories.PaymentRepository;
import com.service.hotelbookingback.services.NotificationService;
import com.service.hotelbookingback.services.PaymentService;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    @Value("${stripe.api.secret.key}")
    private String secretKey;

    @Override
    public String createPaymentIntent(PaymentRequest paymentRequest){
        log.info("Inside createPaymentIntent()");
        Stripe.apiKey = secretKey;
        String bookingReference = paymentRequest.getBookingReference();
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new NotFoundException("Booking Not Found"));
        /*if (booking.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new NotFoundException("Payment already made for this booking");
        }*/
        try{
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(paymentRequest.getAmount().multiply(BigDecimal.valueOf(100)).longValue()) //amount cents
                    .setCurrency("USD")
                    .putMetadata("bookingReference", bookingReference)
                    .build();
            PaymentIntent intent = PaymentIntent.create(params);
            return intent.getClientSecret();
        }catch (Exception e){
            throw new RuntimeException("Error creating payment intent");
        }
    }

    @Override
    public void updatePaymentBooking(PaymentRequest paymentRequest){
        log.info("Inside updatePaymentBooking()");
        String bookingReference = paymentRequest.getBookingReference();
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(()-> new NotFoundException("Booing Not Found"));
        Payment payment = new Payment();
        payment.setPaymentGateway(PaymentGateway.STRIPE);
        payment.setAmount(paymentRequest.getAmount());
        payment.setTransactionId(paymentRequest.getTransactionId());
        //payment.setPaymentStatus(paymentRequest.isSuccess() ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setBookingReference(bookingReference);
        payment.setUser(booking.getUser());
        if (!paymentRequest.isSuccess()) {
            payment.setFailureReason(paymentRequest.getFailureReason());
        }
        paymentRepository.save(payment); //save payment to database
        //create and send notifiaction
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(booking.getUser().getEmail())
                .type(NotificationType.EMAIL)
                .bookingReference(bookingReference)
                .build();
        log.info("About to send notification inside updatePaymentBooking  by sms");
        if (paymentRequest.isSuccess()){
           // booking.setPaymentStatus(PaymentStatus.COMPLETED);
            bookingRepository.save(booking); //Update the booking
            notificationDTO.setSubject("Booking Payment Successful");
            notificationDTO.setBody("Congratulation!! Your payment for booking with reference: " + bookingReference + "is successful");
            notificationService.sendEmail(notificationDTO); //send email
        }else {
            //booking.setPaymentStatus(PaymentStatus.FAILED);
            bookingRepository.save(booking); //Update the booking
            notificationDTO.setSubject("Booking Payment Failed");
            notificationDTO.setBody("Your payment for booking with reference: " + bookingReference + "failed with reason: " + paymentRequest.getFailureReason());
            notificationService.sendEmail(notificationDTO); //send email
        }
    }
}
