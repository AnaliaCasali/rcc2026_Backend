package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.model.Localidad;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

// para usuario comun
public record UsuarioCreateDTO(
    // username sera email
    @NotNull(message = "El email no puede ser nulo")
    @Email(message = "Debe ingresar un correo electrónico válido")
    String username,

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, max = 20, message = "La contraseña debe tener entre " +
        "8 y 20 caracteres")
    String password,

    @NotBlank(message = "El nombre del grupo no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String nombre,

    @Size(max = 500, message = "La biografía no puede superar los 500 caracteres")
    String biografia,

    // únicamente el ID de la entidad Localidad
    @Positive(message = "El ID de la localidad debe ser un número positivo")
    @NotNull(message = "La localidad es obligatoria para un grupo")
    Long localidadId
) {

}
