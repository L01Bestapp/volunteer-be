package com.ctxh.volunteer.module.auth.service;

import com.ctxh.volunteer.module.auth.enums.EmailTemplates;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Slf4j
@Service
public class MailService {
    private final JavaMailSender mailSender;

    @Autowired
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${spring.mail.username}")
    private String from;

    public void sendEmail(String to, String link, EmailTemplates emailTemplates) throws MessagingException, UnsupportedEncodingException {
        log.info("Sending email verification to {}", to);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        if (to.contains(","))
            messageHelper.setTo(InternetAddress.parse(to));
        else
            messageHelper.setTo(to);
        messageHelper.setFrom(from, "Uni Volunteer");
        messageHelper.setSubject(emailTemplates.getSubject());
        String content = emailTemplates.formatContent(link);
        messageHelper.setText(content, true);
        mailSender.send(mimeMessage);
        log.info("Sending email to {} with subject: {}", to, emailTemplates.getSubject());
    }
}
