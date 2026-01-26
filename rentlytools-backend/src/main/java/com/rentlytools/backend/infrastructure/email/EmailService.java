package com.rentlytools.backend.infrastructure.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

public void sendOtpEmail(String to, String name, String otp) {
    try {
        MimeMessage message = mailSender.createMimeMessage();
        // true activates multipart mode for inline attachments
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Context context = new Context();
        context.setVariable("userName", name);
        context.setVariable("otpCode", otp);

        String htmlContent = templateEngine.process("verification-email", context);

        helper.setTo(to);
        helper.setSubject(otp + " is your RentlyTools verification code");
        helper.setFrom("RentlyTools <no-reply@rentlytools.com>");

        // Step 1: Set the HTML text FIRST
        helper.setText(htmlContent, true); 

        // Step 2: Reference the fixed filename logo.png
        ClassPathResource res = new ClassPathResource("static/images/logo.png");
        
        if (res.exists()) {
            // Step 3: Link resource to cid:logoImage
            helper.addInline("logoImage", res, "image/png"); 
        } else {
            // Log helpful error for DevOps troubleshooting
            System.err.println("Build Error: Logo not found in classpath at " + res.getPath());
        }

        mailSender.send(message);
    } catch (Exception e) {
        throw new RuntimeException("Email delivery failed: " + e.getMessage());
    }
}
}

