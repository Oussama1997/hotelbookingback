package com.service.hotelbookingback.services;

import com.service.hotelbookingback.dtos.payment.PaymentRequest;

public interface PaymentService {

    String createPaymentIntent(PaymentRequest paymentRequest);
    void updatePaymentBooking(PaymentRequest paymentRequest);
}
