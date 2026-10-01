package com.ticdiaspora.application.port.out;

public interface EmailSenderPort {
    void send(String to, String subject, String body);
}
