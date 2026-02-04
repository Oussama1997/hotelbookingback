package com.service.hotelbookingback.services;

import com.service.hotelbookingback.dtos.NotificationDTO;

public interface NotificationService {
    void sendEmail(NotificationDTO notificationDTO);
    void sendSms();
    void sendWhatsapp();
}