package com.service.hotelbookingback.dtos.payment;

import com.service.hotelbookingback.enums.PaymentGateway;
import com.service.hotelbookingback.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreatePaymentRequest {
    private String transactionId;
    private String bookingReference;
    private BigDecimal amount;
    private String currency;
    private PaymentGateway paymentGateway;
    private LocalDateTime paymentDate;
    private PaymentStatus status;
    private String failureReason;
}