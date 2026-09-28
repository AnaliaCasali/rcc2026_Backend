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

class CambiarPasswordRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Debe pasar la validación con datos válidos")
    void validacion_Exito() {
        CambiarPasswordRequest dto = new CambiarPasswordRequest(
                "PasswordVieja123",
                "PasswordNueva456",
                "PasswordNueva456"
        );

        Set<ConstraintViolation<CambiarPasswordRequest>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
        assertThat(dto.currentPassword()).isEqualTo("PasswordVieja123");
        assertThat(dto.newPassword()).isEqualTo("PasswordNueva456");
        assertThat(dto.confirmationPassword()).isEqualTo("PasswordNueva456");
    }

    @Test
    @DisplayName("Debe fallar si los campos están en blanco")
    void validacion_CamposEnBlanco() {
        CambiarPasswordRequest dto = new CambiarPasswordRequest("", "", "");

        Set<ConstraintViolation<CambiarPasswordRequest>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("currentPassword"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("newPassword"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("confirmationPassword"));
    }

    @Test
    @DisplayName("Debe fallar si la nueva contraseña tiene menos de 6 caracteres")
    void validacion_PasswordCorta() {
        CambiarPasswordRequest dto = new CambiarPasswordRequest(
                "PasswordVieja123",
                "12345",
                "12345"
        );

        Set<ConstraintViolation<CambiarPasswordRequest>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("al menos 6 caracteres");
    }

}