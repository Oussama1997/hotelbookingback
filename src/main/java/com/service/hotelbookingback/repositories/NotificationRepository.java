package com.service.hotelbookingback.repositories;

import com.service.hotelbookingback.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
