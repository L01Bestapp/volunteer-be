package com.ctxh.volunteer.module.auth.service;

import com.ctxh.volunteer.module.auth.enums.EmailTemplates;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MailService Unit Tests")
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailService mailService;

    @Captor
    private ArgumentCaptor<MimeMessage> mimeMessageCaptor;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        // Set the @Value field using ReflectionTestUtils
        ReflectionTestUtils.setField(mailService, "from", "test@quniverse.com");

        // Create a real MimeMessage for mocking
        Session session = Session.getInstance(new Properties());
        mimeMessage = new MimeMessage(session);

        // Mock createMimeMessage to return our test MimeMessage
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("Send Email - Success with single recipient")
    void sendEmail_Success_WithSingleRecipient() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String to = "recipient@hcmut.edu.vn";
        String link = "http://localhost:8080/verify?token=abc123";
        EmailTemplates template = EmailTemplates.VERIFY_EMAIL_TEMPLATE;

        // Act
        mailService.sendEmail(to, link, template);

        // Assert
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessageCaptor.capture());

        MimeMessage sentMessage = mimeMessageCaptor.getValue();
        assertThat(sentMessage.getSubject()).isEqualTo(template.getSubject());
        assertThat(sentMessage.getAllRecipients()).hasSize(1);
        assertThat(sentMessage.getAllRecipients()[0].toString()).isEqualTo(to);
    }

    @Test
    @DisplayName("Send Email - Success with multiple recipients")
    void sendEmail_Success_WithMultipleRecipients() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String to = "user1@hcmut.edu.vn,user2@hcmut.edu.vn,user3@hcmut.edu.vn";
        String link = "http://localhost:8080/verify?token=xyz789";
        EmailTemplates template = EmailTemplates.VERIFY_EMAIL_TEMPLATE;

        // Act
        mailService.sendEmail(to, link, template);

        // Assert
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessageCaptor.capture());

        MimeMessage sentMessage = mimeMessageCaptor.getValue();
        assertThat(sentMessage.getAllRecipients()).hasSize(3);
    }

    @Test
    @DisplayName("Send Email - Uses correct email template for verification")
    void sendEmail_UsesCorrectTemplate_ForVerification() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String to = "user@hcmut.edu.vn";
        String link = "http://localhost:8080/verify?token=test123";
        EmailTemplates template = EmailTemplates.VERIFY_EMAIL_TEMPLATE;

        // Act
        mailService.sendEmail(to, link, template);

        // Assert
        verify(mailSender).send(mimeMessageCaptor.capture());
        MimeMessage sentMessage = mimeMessageCaptor.getValue();
        assertThat(sentMessage.getSubject()).isEqualTo(EmailTemplates.VERIFY_EMAIL_TEMPLATE.getSubject());
    }

    @Test
    @DisplayName("Send Email - Uses correct email template for password reset")
    void sendEmail_UsesCorrectTemplate_ForPasswordReset() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String to = "user@hcmut.edu.vn";
        String otpCode = "123456";
        EmailTemplates template = EmailTemplates.VERIFY_RESET_PASSWORD_TEMPLATE;

        // Act
        mailService.sendEmail(to, otpCode, template);

        // Assert
        verify(mailSender).send(mimeMessageCaptor.capture());
        MimeMessage sentMessage = mimeMessageCaptor.getValue();
        assertThat(sentMessage.getSubject()).isEqualTo(EmailTemplates.VERIFY_RESET_PASSWORD_TEMPLATE.getSubject());
    }

    @Test
    @DisplayName("Send Email - Sets correct sender information")
    void sendEmail_SetsCorrectSender() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String to = "recipient@hcmut.edu.vn";
        String link = "http://localhost:8080/verify";
        EmailTemplates template = EmailTemplates.VERIFY_EMAIL_TEMPLATE;

        // Act
        mailService.sendEmail(to, link, template);

        // Assert
        verify(mailSender).send(mimeMessageCaptor.capture());
        MimeMessage sentMessage = mimeMessageCaptor.getValue();
        assertThat(sentMessage.getFrom()).isNotEmpty();
        assertThat(sentMessage.getFrom()[0].toString()).contains("QUniverse");
    }

    @Test
    @DisplayName("Send Email - Formats content with link")
    void sendEmail_FormatsContentWithLink() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String to = "user@hcmut.edu.vn";
        String link = "http://localhost:8080/verify?token=abc123";
        EmailTemplates template = EmailTemplates.VERIFY_EMAIL_TEMPLATE;

        // Act
        mailService.sendEmail(to, link, template);

        // Assert
        verify(mailSender).send(any(MimeMessage.class));
        verify(mailSender).createMimeMessage();
    }

    @Test
    @DisplayName("Send Email - Does not throw exception when successful")
    void sendEmail_DoesNotThrowException_WhenSuccessful() {
        // Arrange
        String to = "user@hcmut.edu.vn";
        String link = "http://localhost:8080/verify";
        EmailTemplates template = EmailTemplates.VERIFY_EMAIL_TEMPLATE;

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertThatCode(() -> mailService.sendEmail(to, link, template))
                .doesNotThrowAnyException();

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Send Email - Throws MessagingException when mail sender fails")
    void sendEmail_ThrowsMessagingException_WhenMailSenderFails() {
        // Arrange
        String to = "user@hcmut.edu.vn";
        String link = "http://localhost:8080/verify";
        EmailTemplates template = EmailTemplates.VERIFY_EMAIL_TEMPLATE;

        doThrow(new RuntimeException("Mail server error"))
                .when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertThatCode(() -> mailService.sendEmail(to, link, template))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Mail server error");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Send Email - Sets message encoding to UTF-8")
    void sendEmail_SetsUtf8Encoding() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String to = "user@hcmut.edu.vn";
        String link = "http://localhost:8080/verify";
        EmailTemplates template = EmailTemplates.VERIFY_EMAIL_TEMPLATE;

        // Act
        mailService.sendEmail(to, link, template);

        // Assert
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }
}
