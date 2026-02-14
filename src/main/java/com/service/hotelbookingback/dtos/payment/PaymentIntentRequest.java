package com.service.hotelbookingback.dtos.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentIntentRequest {
    private BigDecimal amount;
    private String reference;
    private String currency;
}