package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    @Override
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            String resetUrl = "http://localhost:4200/reset-password?token=" + resetToken;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Recuperación de Contraseña");
            message.setText("Hola,\n\n"
                    + "Recibimos una solicitud para restablecer la contraseña de tu cuenta.\n"
                    + "Haz clic en el siguiente enlace para continuar:\n\n"
                    + resetUrl + "\n\n"
                    + "Este enlace tiene una validez de 15 minutos.\n"
                    + "Si no solicitaste este cambio, puedes ignorar este mensaje.");

            mailSender.send(message);
            log.info("Correo de restablecimiento enviado exitosamente a: {}", toEmail);
        } catch (Exception e) {
            log.error("Error al enviar correo a {}: {}", toEmail, e.getMessage());
        }
    }
}