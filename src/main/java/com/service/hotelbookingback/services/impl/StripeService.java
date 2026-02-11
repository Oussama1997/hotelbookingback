package com.service.hotelbookingback.services.impl;

import com.service.hotelbookingback.dtos.payment.CreatePaymentRequest;
import com.service.hotelbookingback.dtos.payment.PaymentIntentResponse;
import com.service.hotelbookingback.entities.Payment;
import com.service.hotelbookingback.enums.PaymentStatus;
import com.service.hotelbookingback.repositories.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StripeService {

    private final PaymentRepository paymentRepository;

    public PaymentIntentResponse createPaymentIntent(Long amount, String currency) throws StripeException {

        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount); // cents
        params.put("currency", currency);
        params.put("payment_method_types", List.of("card"));

        PaymentIntent intent = PaymentIntent.create(params);
        return new PaymentIntentResponse(intent.getClientSecret());
    }

    public Payment savePayment(CreatePaymentRequest request) {

        Payment payment = Payment.builder()
                .transactionId(request.getTransactionId())
                .bookingReference(request.getBookingReference())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentGateway(request.getPaymentGateway())
                .status(PaymentStatus.COMPLETED)
                .build();

        return paymentRepository.save(payment);
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

