package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.model.Localidad;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// para grupos de danza/artista
public record GrupoCreateDTO(
    // username sera email
    @Email(message = "Debe ingresar un correo electrónico válido")
    String username,

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, max = 20, message = "La contraseña debe tener entre " +
        "8 y 20 caracteres")
    String password,

    Role role,

    String nombre,

    String biografia,

    Localidad localidad
)
{
}
