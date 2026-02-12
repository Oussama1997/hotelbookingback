package com.service.hotelbookingback.services.impl;

import com.service.hotelbookingback.dtos.payment.CreatePaymentRequest;
import com.service.hotelbookingback.dtos.payment.PaymentIntentRequest;
import com.service.hotelbookingback.dtos.payment.PaymentIntentResponse;
import com.service.hotelbookingback.entities.Booking;
import com.service.hotelbookingback.entities.Payment;
import com.service.hotelbookingback.entities.User;
import com.service.hotelbookingback.enums.BookingStatus;
import com.service.hotelbookingback.enums.EmailTemplate;
import com.service.hotelbookingback.enums.PaymentStatus;
import com.service.hotelbookingback.repositories.BookingRepository;
import com.service.hotelbookingback.repositories.PaymentRepository;
import com.service.hotelbookingback.services.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StripeService {

    private final PaymentRepository paymentRepository;
    private final EmailServiceImpl emailService;
    private final UserService userService;
    private final BookingRepository bookingRepository;

    public PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request) throws StripeException {

        Booking booking = bookingRepository.findByBookingReference(request.getBookingReference())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getBookingStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Booking is no longer payable");
        }

        if (booking.getPaymentDeadline().isBefore(LocalDateTime.now())) {
            booking.setBookingStatus(BookingStatus.CANCELLED);
            booking.setPaymentStatus(PaymentStatus.EXPIRED);
            bookingRepository.save(booking);
            throw new RuntimeException("Payment time expired");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("amount", request.getAmount()); // cents
        params.put("currency", request.getCurrency());
        params.put("payment_method_types", List.of("card"));

        PaymentIntent intent = PaymentIntent.create(params);
        return new PaymentIntentResponse(intent.getClientSecret());
    }

    public Payment savePayment(CreatePaymentRequest request) {
        User currentUser = userService.getCurrentLoggedInUser();
        Payment payment = Payment.builder()
                .user(currentUser)
                .transactionId(request.getTransactionId())
                .bookingReference(request.getBookingReference())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentGateway(request.getPaymentGateway())
                .paymentDate(request.getPaymentDate())
                .status(request.getStatus())
                .failureReason(request.getFailureReason())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        this.sendNotifPayConf(savedPayment);
        return savedPayment;
    }

    private void sendNotifPayConf(Payment savedPayment){
        User currentUser = userService.getCurrentLoggedInUser();
        String bookingUrl = "http://localhost:4200/booking/payment/" + savedPayment.getBookingReference();
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", currentUser.getLastName() + " " + currentUser.getFirstName());
        vars.put("bookingRef", savedPayment.getTransactionId());
        vars.put("transactionId", savedPayment.getTransactionId());
        vars.put("amount", savedPayment.getAmount());
        vars.put("bookingUrl", bookingUrl);
        emailService.sendTemplateEmail(currentUser.getEmail(),
                EmailTemplate.PAYMENT_CONFIRMATION, vars);// sending email
    }

    public List<Payment> getAll() {
        return paymentRepository.findAll();
    }

    public Optional<Payment> getById(Long id) {
        return paymentRepository.findById(id);
    }

    public Optional<Payment> getByBooking(String ref) {
        return paymentRepository.findByBookingReference(ref);
    }

    public Payment refundPayment(Long id) throws StripeException {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(payment.getTransactionId())
                .build();

        Refund.create(params);

        payment.setStatus(PaymentStatus.REFUNDED);
        return paymentRepository.save(payment);
    }
}

