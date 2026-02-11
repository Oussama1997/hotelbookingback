package com.service.hotelbookingback.dtos.payment;

import com.service.hotelbookingback.enums.PaymentGateway;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
    private String transactionId;
    private String bookingReference;
    private BigDecimal amount;
    private String currency;
    private PaymentGateway paymentGateway;
}