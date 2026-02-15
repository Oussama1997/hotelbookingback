package com.service.hotelbookingback.services.impl;

import com.service.hotelbookingback.dtos.ApiResponse;
import com.service.hotelbookingback.dtos.payment.CreatePaymentRequest;
import com.service.hotelbookingback.dtos.payment.PaymentDTO;
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
import com.service.hotelbookingback.services.interfaces.PaymentService;
import com.service.hotelbookingback.services.interfaces.UserService;
import com.service.hotelbookingback.utils.Converter;
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

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final EmailServiceImpl emailService;
    private final UserService userService;
    private final BookingRepository bookingRepository;

    @Override
    public ApiResponse<PaymentIntentResponse> createPaymentIntent(PaymentIntentRequest request) {

        Booking booking = bookingRepository.findByReference(request.getReference())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Booking already paid");
        }
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Booking is no longer payable");
        }
        if (booking.getPaymentDeadline().isBefore(LocalDateTime.now())) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setPaymentStatus(PaymentStatus.EXPIRED);
            bookingRepository.save(booking);
            throw new RuntimeException("Payment time expired");
        }
        if(booking.getPaymentStatus() != PaymentStatus.PENDING){
            booking.setPaymentStatus(PaymentStatus.PROCESSING);
            bookingRepository.save(booking);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("amount", request.getAmount()); // cents
        params.put("currency", request.getCurrency());
        params.put("payment_method_types", List.of("card"));

        PaymentIntent intent = null;
        try {
            intent = PaymentIntent.create(params);
        } catch (StripeException e) {
            throw new RuntimeException("PaymentIntent failed");
        }
        return ApiResponse.<PaymentIntentResponse>builder()
                .status(200)
                .message("success")
                .data(new PaymentIntentResponse(intent.getClientSecret()))
                .build();
    }

    @Override
    public ApiResponse<PaymentDTO> savePayment(CreatePaymentRequest request) {
        User currentUser = userService.getCurrentLoggedInUser();
        Payment payment = Payment.builder()
                .user(currentUser)
                .transactionId(request.getTransactionId())
                .reference(request.getReference())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .method(request.getMethod())
                .date(request.getDate())
                .status(request.getStatus())
                .failureReason(request.getFailureReason())
                .build();
        Payment savedPayment = paymentRepository.save(payment);
        Booking booking = bookingRepository.findByReference(request.getReference())
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
            if(payment.getStatus().equals(PaymentStatus.PAID)){
                booking.setStatus(BookingStatus.CONFIRMED);
                booking.setPaymentStatus(PaymentStatus.PAID);
            } else if(payment.getStatus().equals(PaymentStatus.FAILED)){
                booking.setStatus(BookingStatus.CANCELLED);
                booking.setPaymentStatus(PaymentStatus.FAILED);
            }
            bookingRepository.save(booking);
        }
        this.sendNotifPayment(savedPayment);
        return ApiResponse.<PaymentDTO>builder()
                .status(200)
                .message("success")
                .data(Converter.convertToResponseDTO(savedPayment))
                .build();
    }

    private void sendNotifPayment(Payment savedPayment){
        User currentUser = userService.getCurrentLoggedInUser();
        String bookingUrl = "http://localhost:4200/booking/payment/" + savedPayment.getReference();
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", currentUser.getLastName() + " " + currentUser.getFirstName());
        vars.put("bookingRef", savedPayment.getTransactionId());
        vars.put("transactionId", savedPayment.getTransactionId());
        vars.put("amount", savedPayment.getAmount());
        vars.put("bookingUrl", bookingUrl);
        if(savedPayment.getStatus().equals(PaymentStatus.PAID)) {
            emailService.sendTemplateEmail(currentUser.getEmail(),
                    EmailTemplate.PAYMENT_CONFIRMATION, vars);// sending email
        }else{
            // Failed || Expired
            emailService.sendTemplateEmail(currentUser.getEmail(),
                    EmailTemplate.PAYMENT_FAILED, vars);// sending email
        }
    }

    @Override
    public ApiResponse<List<PaymentDTO>> getAllPayments() {
        List<PaymentDTO> paymentDTOList = paymentRepository.findAll().stream()
                .map(Converter::convertToResponseDTO)
                .toList();
        return ApiResponse.<List<PaymentDTO>>builder()
                .status(200)
                .message("Success")
                .data(paymentDTOList)
                .build();
    }

    @Override
    public ApiResponse<PaymentDTO> getById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return ApiResponse.<PaymentDTO>builder()
                .status(200)
                .message("Success")
                .data(Converter.convertToResponseDTO(payment))
                .build();
    }

    @Override
    public ApiResponse<PaymentDTO> getByBooking(String ref) {
        Payment payment = paymentRepository.findByReference(ref)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return ApiResponse.<PaymentDTO>builder()
                .status(200)
                .message("Success")
                .data(Converter.convertToResponseDTO(payment))
                .build();
    }

    @Override
    public ApiResponse<PaymentDTO> refundPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(payment.getTransactionId())
                .build();
        try {
            Refund.create(params);
        } catch (StripeException e) {
            throw new RuntimeException("Refund failed");
        }
        payment.setStatus(PaymentStatus.REFUNDED);
        Payment savedPayment = paymentRepository.save(payment);
        return ApiResponse.<PaymentDTO>builder()
                .status(200)
                .message("Success")
                .data(Converter.convertToResponseDTO(savedPayment))
                .build();
    }

    @Override
    public boolean refundIfPaid(Booking booking) {
        Payment payment = paymentRepository.findByReference(booking.getReference())
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        if (payment.getStatus() != PaymentStatus.PAID) return false;
        try {
            Refund refund = Refund.create(
                    RefundCreateParams.builder()
                            .setPaymentIntent(payment.getTransactionId())
                            .build()
            );
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        } catch (StripeException e) {
            throw new RuntimeException("Refund failed");
        }
        return true;
    }
}