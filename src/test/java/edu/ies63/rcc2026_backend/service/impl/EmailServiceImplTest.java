package edu.ies63.rcc2026_backend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        // Asignamos el valor al campo privado @Value("${spring.mail.username}")
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@rcc2026.edu.ar");
    }

    @Test
    @DisplayName("Envío exitoso: construye y envía el mensaje con el token correcto")
    void sendPasswordResetEmail_Exitoso() {
        String destinatario = "usuario@test.com";
        String token = "abc-123-token";

        emailService.sendPasswordResetEmail(destinatario, token);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getFrom()).isEqualTo("noreply@rcc2026.edu.ar");
        assertThat(capturedMessage.getTo()).containsExactly(destinatario);
        assertThat(capturedMessage.getSubject()).isEqualTo("Recuperación de Contraseña");
        assertThat(capturedMessage.getText())
                .contains("http://localhost:4200/reset-password?token=abc-123-token")
                .contains("15 minutos");
    }

    @Test
    @DisplayName("Manejo de excepción: si JavaMailSender falla, entra al catch y no interrumpe el flujo")
    void sendPasswordResetEmail_FallaEnvio_EntraEnCatch() {
        String destinatario = "usuario@test.com";
        String token = "abc-123-token";

        doThrow(new MailSendException("Fallo de conexión SMTP"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // No debe lanzar excepción hacia arriba porque está contenido en el try-catch
        emailService.sendPasswordResetEmail(destinatario, token);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}