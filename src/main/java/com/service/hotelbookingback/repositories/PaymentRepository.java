package com.service.hotelbookingback.repositories;

import com.service.hotelbookingback.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByReference(String Reference);
}