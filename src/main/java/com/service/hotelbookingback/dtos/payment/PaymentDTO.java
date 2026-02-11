package com.service.hotelbookingback.dtos.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.service.hotelbookingback.dtos.BookingDTO;
import com.service.hotelbookingback.enums.PaymentGateway;
import com.service.hotelbookingback.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentDTO {

    private Long id;
    private BookingDTO booking;
    private Long transactionId;
    private BigDecimal amount;
    private PaymentGateway paymentMethod; //e,g Paypal. Stripe, flutterwave, paystack
    private LocalDateTime paymentDate;
    private PaymentStatus status;
    private String bookingReference;
    private String failureReason;
    private String approvalLink; //paypal payment approval UEL
}
