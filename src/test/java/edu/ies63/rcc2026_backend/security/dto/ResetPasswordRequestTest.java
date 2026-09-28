package edu.ies63.rcc2026_backend.security.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ResetPasswordRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Debe ser válido cuando todos los campos cumplen las condiciones")
    void validacion_Exito() {
        ResetPasswordRequest dto = new ResetPasswordRequest(
                "token-uuid-1234",
                "PasswordSegura123!",
                "PasswordSegura123!"
        );

        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
        assertThat(dto.token()).isEqualTo("token-uuid-1234");
        assertThat(dto.newPassword()).isEqualTo("PasswordSegura123!");
        assertThat(dto.confirmationPassword()).isEqualTo("PasswordSegura123!");
    }

    @Test
    @DisplayName("Debe fallar si falta el token o la confirmación está vacía")
    void validacion_CamposEnBlanco() {
        ResetPasswordRequest dto = new ResetPasswordRequest(
                "",
                "Password123!",
                ""
        );

        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(dto);

        assertThat(violations).hasSize(2);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("token"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("confirmationPassword"));
    }

    @Test
    @DisplayName("Debe fallar si la nueva contraseña tiene menos de 6 caracteres")
    void validacion_PasswordCorta() {
        ResetPasswordRequest dto = new ResetPasswordRequest(
                "token-uuid-1234",
                "123",
                "123"
        );

        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("al menos 6 caracteres");
    }
}