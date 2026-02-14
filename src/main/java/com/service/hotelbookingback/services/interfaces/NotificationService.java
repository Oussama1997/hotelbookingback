package com.service.hotelbookingback.services.interfaces;

import com.service.hotelbookingback.dtos.EmailNotificationRequest;

public interface NotificationService {
    void sendEmail(EmailNotificationRequest request);
    void sendSms();
    void sendWhatsapp();
}