package com.service.hotelbookingback.dtos;

import com.service.hotelbookingback.enums.EmailTemplate;
import lombok.Data;

import java.util.Map;

@Data
public class EmailNotificationRequest {
    private String to;
    private String subject;
    private String body;
    private EmailTemplate templateType;
    private Map<String, Object> data;
}