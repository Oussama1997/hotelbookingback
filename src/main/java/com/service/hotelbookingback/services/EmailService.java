package com.service.hotelbookingback.services;

import com.service.hotelbookingback.enums.EmailTemplate;

import java.util.Map;

public interface EmailService {

    void sendCustomEmail(String to, String from, String subject,
                        String body, Map<String, Object> variables);

    void sendTemplateEmail(String to,
                           EmailTemplate template,
                           Map<String, Object> variables);
}
