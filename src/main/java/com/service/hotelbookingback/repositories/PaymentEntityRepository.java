package com.service.hotelbookingback.repositories;

import com.service.hotelbookingback.entities.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentEntityRepository extends JpaRepository<PaymentEntity, Long> {}
