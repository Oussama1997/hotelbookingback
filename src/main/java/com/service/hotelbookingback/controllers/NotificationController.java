package com.service.hotelbookingback.controllers;

import com.service.hotelbookingback.dtos.EmailNotificationRequest;
import com.service.hotelbookingback.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Generic email
    @PostMapping("/email")
    public String sendEmail(@RequestBody EmailNotificationRequest request) {
        notificationService.sendEmail(request);
        return "Email sent";
    }
}