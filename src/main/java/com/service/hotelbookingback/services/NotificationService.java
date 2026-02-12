package com.service.hotelbookingback.services;

import com.service.hotelbookingback.dtos.NotificationDTO;
import com.service.hotelbookingback.dtos.email.EmailNotificationRequest;

public interface NotificationService {
    void sendEmail(EmailNotificationRequest request);
    void sendSms();
    void sendWhatsapp();
}