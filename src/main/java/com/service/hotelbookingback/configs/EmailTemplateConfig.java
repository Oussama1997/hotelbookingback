package com.service.hotelbookingback.configs;

import com.service.hotelbookingback.enums.EmailTemplate;
import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Getter
public class EmailTemplateConfig {

    @Value("${spring.mail.username}")
    public String companyEmail;

    private final Map<EmailTemplate, String> templateFiles = Map.of(
            EmailTemplate.SEND_EMAIL, "send-email",
            EmailTemplate.BOOKING_CREATION, "booking-creation",
            EmailTemplate.BOOKING_CANCELLATION, "booking-cancellation",
            EmailTemplate.PAYMENT_CONFIRMATION, "payment-confirmation",
            EmailTemplate.PAYMENT_FAILED, "payment-failed",
            EmailTemplate.WELCOME, "welcome",
            EmailTemplate.PASSWORD_RESET, "password-reset",
            EmailTemplate.CHECKED_IN_REMINDER, "check-in-reminder"
    );

    private final Map<EmailTemplate, String> subjects = Map.of(
            EmailTemplate.BOOKING_CREATION, " Booking Created",
            EmailTemplate.BOOKING_CANCELLATION, "Booking Cancelled",
            EmailTemplate.PAYMENT_CONFIRMATION, "💳 Payment Successful",
            EmailTemplate.PAYMENT_FAILED, "⚠️ Payment Failed",
            EmailTemplate.WELCOME, "Welcome to MareMonte 🎉",
            EmailTemplate.PASSWORD_RESET, "Reset your password",
            EmailTemplate.CHECKED_IN_REMINDER, "⏰ Check-in Reminder"
    );
}
