package com.service.hotelbookingback.services.impl;

import com.service.hotelbookingback.dtos.email.EmailNotificationRequest;
import com.service.hotelbookingback.entities.User;
import com.service.hotelbookingback.enums.EmailTemplate;
import com.service.hotelbookingback.services.EmailService;
import com.service.hotelbookingback.services.NotificationService;
import com.service.hotelbookingback.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final UserService userService;
    private final EmailService emailService;

    @Override
    @Async
    public void sendEmail(EmailNotificationRequest request) {
        log.info("Sending email ...");
         if (request.getTemplateType() == EmailTemplate.SEND_EMAIL){
            emailService.sendCustomEmail(request.getTo(),
                    request.getTo(), request.getSubject(),
                    request.getBody(), request.getData());
        } else {
            User user = userService.getCurrentLoggedInUser();
            request.getData().put("name", user.getLastName() + " " + user.getFirstName());
            emailService.sendTemplateEmail(user.getEmail(), request.getTemplateType(), request.getData());
        }
    }

    @Override
    public void sendSms() {

    }

    @Override
    public void sendWhatsapp() {
    }
}