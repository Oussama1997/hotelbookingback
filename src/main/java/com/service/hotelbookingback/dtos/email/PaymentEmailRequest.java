package com.service.hotelbookingback.dtos.email;

import lombok.Data;

@Data
public class PaymentEmailRequest {
    private String email;
    private String bookingReference;
    private String transactionId;
    private Double amount;
    private String currency;
}