package com.service.hotelbookingback.dtos.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.service.hotelbookingback.dtos.BookingDTO;
import com.service.hotelbookingback.dtos.UserDTO;
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
    private UserDTO user;
    private Long transactionId;
    private BigDecimal amount;
    private String currency;
    private PaymentGateway method;
    private LocalDateTime date;
    private PaymentStatus status;
    private String reference;
    private String failureReason;
    private String approvalLink;
}
