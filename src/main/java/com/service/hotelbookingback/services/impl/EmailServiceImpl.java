package com.service.hotelbookingback.services.impl;

import com.service.hotelbookingback.configs.EmailTemplateConfig;
import com.service.hotelbookingback.enums.EmailTemplate;
import com.service.hotelbookingback.services.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final EmailTemplateConfig templateConfig;

    @Override
    public void sendCustomEmail(String to, String from, String subject,
                                String body, Map<String, Object> variables){
        try {
            // get template file
            String templateFile = templateConfig.getTemplateFiles().get(EmailTemplate.SEND_EMAIL);

            // inject variables into template
            Context context = new Context();
            context.setVariables(variables);

            String html = templateEngine.process("emails/" + templateFile, context);

            // send email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setFrom(from);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + EmailTemplate.SEND_EMAIL, e);
        }
    }

    @Override
    public void sendTemplateEmail(String to,
                                  EmailTemplate template,
                                  Map<String, Object> variables) {

        try {
            // get template file & subject automatically
            String templateFile = templateConfig.getTemplateFiles().get(template);
            String subject = templateConfig.getSubjects().get(template);

            // inject variables into template
            Context context = new Context();
            context.setVariables(variables);

            String html = templateEngine.process("emails/" + templateFile, context);

            // send email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setFrom(templateConfig.getCompanyEmail());

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + template.name(), e);
        }
    }
}
