package com.service.hotelbookingback.services.impl;

import com.service.hotelbookingback.dtos.NotificationDTO;
import com.service.hotelbookingback.entities.Notification;
import com.service.hotelbookingback.enums.NotificationType;
import com.service.hotelbookingback.repositories.NotificationRepository;
import com.service.hotelbookingback.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender javaMailSender;
    private final NotificationRepository notificationRepository;

    @Override
    @Async
    public void sendEmail(NotificationDTO notificationDTO) {
        log.info("Sending email ...");
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(notificationDTO.getRecipient());
        simpleMailMessage.setSubject(notificationDTO.getSubject());
        simpleMailMessage.setText(notificationDTO.getBody());
        javaMailSender.send(simpleMailMessage);
        //SAVE TO DATABSE
        Notification notificationToSave = Notification.builder()
                .recipient(notificationDTO.getRecipient())
                .subject(notificationDTO.getSubject())
                .body(notificationDTO.getBody())
                .bookingReference(notificationDTO.getBookingReference())
                .type(NotificationType.EMAIL)
                .build();
        notificationRepository.save(notificationToSave);
    }

    @Override
    public void sendSms() {

    }

    @Override
    public void sendWhatsapp() {
    }
}