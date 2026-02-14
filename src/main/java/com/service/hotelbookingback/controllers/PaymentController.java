package com.service.hotelbookingback.controllers;

import com.service.hotelbookingback.dtos.ApiResponse;
import com.service.hotelbookingback.dtos.payment.CreatePaymentRequest;
import com.service.hotelbookingback.dtos.payment.PaymentDTO;
import com.service.hotelbookingback.dtos.payment.PaymentIntentRequest;
import com.service.hotelbookingback.dtos.payment.PaymentIntentResponse;
import com.service.hotelbookingback.services.impl.PaymentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    //private final PaymentService paymentService;
    private final PaymentServiceImpl paymentService;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<ApiResponse<PaymentIntentResponse>> createPaymentIntent(@RequestBody PaymentIntentRequest request) throws Exception {
        return ResponseEntity.ok(paymentService.createPaymentIntent(request));
    }

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<PaymentDTO>> processPayment(@RequestBody CreatePaymentRequest request) {
        return ResponseEntity.ok(paymentService.savePayment(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getById(id));

    }

    @GetMapping("/booking/{ref}")
    public ResponseEntity<ApiResponse<PaymentDTO>> getByBooking(@PathVariable String ref) {
        return ResponseEntity.ok(paymentService.getByBooking(ref));

    }

    @GetMapping("/all")
    public ApiResponse<List<PaymentDTO>> getAll() {
        return paymentService.getAllPayments();
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<PaymentDTO>> refund(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(paymentService.refundPayment(id));
    }

}
