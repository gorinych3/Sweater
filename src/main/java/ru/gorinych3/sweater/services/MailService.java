package ru.gorinych3.sweater.services;

public interface MailService {

    void send(String emailTo, String subject, String message);
}
