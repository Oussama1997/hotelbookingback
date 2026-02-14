package com.service.hotelbookingback.services.interfaces;

import com.service.hotelbookingback.dtos.ApiResponse;
import com.service.hotelbookingback.dtos.payment.CreatePaymentRequest;
import com.service.hotelbookingback.dtos.payment.PaymentDTO;
import com.service.hotelbookingback.dtos.payment.PaymentIntentRequest;
import com.service.hotelbookingback.dtos.payment.PaymentIntentResponse;
import com.service.hotelbookingback.entities.Booking;

import java.util.List;

public interface PaymentService {

    ApiResponse<PaymentIntentResponse> createPaymentIntent(PaymentIntentRequest request);
    ApiResponse<PaymentDTO> savePayment(CreatePaymentRequest request);
    ApiResponse<List<PaymentDTO>> getAllPayments();
    ApiResponse<PaymentDTO> getById(Long id);
    ApiResponse<PaymentDTO> getByBooking(String ref);
    ApiResponse<PaymentDTO> refundPayment(Long id);
    boolean refundIfPaid(Booking booking);
}
