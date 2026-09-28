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

class ForgotPasswordRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Debe ser válido con un correo o usuario informado")
    void validacion_Exito() {
        ForgotPasswordRequest dto = new ForgotPasswordRequest("usuario@ejemplo.com");

        Set<ConstraintViolation<ForgotPasswordRequest>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
        assertThat(dto.email()).isEqualTo("usuario@ejemplo.com");
    }

    @Test
    @DisplayName("Debe fallar cuando el identificador está en blanco o nulo")
    void validacion_EmailEnBlanco() {
        ForgotPasswordRequest dto = new ForgotPasswordRequest("   ");

        Set<ConstraintViolation<ForgotPasswordRequest>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("obligatorio");
    }

}