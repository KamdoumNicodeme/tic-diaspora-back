package com.ticdiaspora.infrastructure.adapter.out.client;

import com.ticdiaspora.application.port.out.EmailSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpEmailSenderAdapter implements EmailSenderPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpEmailSenderAdapter.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final boolean enabled;
    private final String from;

    public SmtpEmailSenderAdapter(
            ObjectProvider<JavaMailSender> mailSender,
            @Value("${app.mail.enabled:false}") boolean enabled,
            @Value("${app.mail.from:no-reply@ticdiaspora.org}") String from
    ) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
    }

    @Override
    public void send(String to, String subject, String body) {
        if (!enabled) {
            LOGGER.info("Email disabled; would send to={} subject={}", to, subject);
            return;
        }
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            LOGGER.warn("Email enabled but no JavaMailSender bean is available; to={} subject={}", to, subject);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        try {
            sender.send(message);
        } catch (MailException exception) {
            LOGGER.error("Unable to send email to={} subject={}", to, subject, exception);
        }
    }
}
