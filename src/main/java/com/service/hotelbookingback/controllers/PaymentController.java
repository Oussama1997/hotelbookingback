package com.service.hotelbookingback.controllers;

import com.service.hotelbookingback.dtos.payment.CreatePaymentRequest;
import com.service.hotelbookingback.dtos.payment.PaymentIntentRequest;
import com.service.hotelbookingback.entities.Payment;
import com.service.hotelbookingback.services.impl.StripeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    //private final PaymentService paymentService;
    private final StripeService stripeService;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody PaymentIntentRequest request) throws Exception {
        return ResponseEntity.ok(stripeService.createPaymentIntent(request));
    }

    @PostMapping("/process")
    public ResponseEntity<Payment> processPayment(@RequestBody CreatePaymentRequest request) {
        return ResponseEntity.ok(stripeService.savePayment(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPayment(@PathVariable Long id) {
        return stripeService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/booking/{ref}")
    public ResponseEntity<Payment> getByBooking(@PathVariable String ref) {
        return stripeService.getByBooking(ref)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public List<Payment> getAll() {
        return stripeService.getAll();
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<Payment> refund(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(stripeService.refundPayment(id));
    }

}
