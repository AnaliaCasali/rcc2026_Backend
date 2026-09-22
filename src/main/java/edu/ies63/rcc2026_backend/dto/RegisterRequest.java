package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email ingresado no es válido")
    String username,

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    String password,

    @NotBlank(message = "Debes ingresar tu nombre")
    String nombre,

    @NotNull(message = "El rol es requerido")
    Role role,

    // opcionales por si el rol es organizador o grupo
    String biografia,
    String director,
    Long localidadId
) {}
