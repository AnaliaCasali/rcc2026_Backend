package edu.ies63.rcc2026_backend.service;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String resetToken);
    void sendVerificationEmail(String toEmail, String token);
}