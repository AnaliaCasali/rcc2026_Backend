package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.model.Localidad;
import edu.ies63.rcc2026_backend.model.Post;
import edu.ies63.rcc2026_backend.model.Usuario;

import java.util.List;

public record BusquedaResponseDTO(
        List<Usuario> perfiles,
        List<Post> posts,
        List<Localidad> localidades
) {}