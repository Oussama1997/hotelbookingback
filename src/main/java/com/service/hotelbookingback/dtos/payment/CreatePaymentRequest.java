package com.service.hotelbookingback.dtos.payment;

import lombok.Data;

@Data
public class CreatePaymentRequest {
    private String transactionId;
    private String bookingReference;
    private Double amount;
    private String currency;
    private String paymentGateway;
}