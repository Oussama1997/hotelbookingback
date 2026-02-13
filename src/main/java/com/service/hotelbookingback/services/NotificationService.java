package com.service.hotelbookingback.services;

import com.service.hotelbookingback.dtos.EmailNotificationRequest;

public interface NotificationService {
    void sendEmail(EmailNotificationRequest request);
    void sendSms();
    void sendWhatsapp();
}