package edu.ies63.rcc2026_backend.tools;

import edu.ies63.rcc2026_backend.dto.LocalidadResponseDTO;
import edu.ies63.rcc2026_backend.dto.PostResponseDTO;
import edu.ies63.rcc2026_backend.service.LocalidadService;
import edu.ies63.rcc2026_backend.service.PostService;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RccAppTools {

    private final LocalidadService localidadService;
    private final PostService postService;

    private static final Map<String,String> SECCIONES=
            Map.of("acerca", "RCC es Red Cultural Conecta 2026",
                    "contacto","redculturalconecta@gmail.com",
                    "desarrollo","Equipo 3º Año TSDS 2026. ISP 63. ");


    @Tool(description="Devuelve el texto de una sección del sitio web: "
            + "acerca, contacto y desarrolladores")
    public String textoDeLaWeb(@ToolParam (description="nombre de la seccion") String seccion)
    {
        return SECCIONES.getOrDefault(seccion.toLowerCase().trim(),"Sección Inexistente. Opciones: " + SECCIONES.keySet());
    }

    @Tool(description = "Busca localidades registradas cuyo nombre "
            + "contenga el texto indicado")
    public List<String> buscarLocalidades(
            @ToolParam(description = "texto a buscar") String texto) {
        return localidadService.listarLocalidades().stream()
                .map(LocalidadResponseDTO::localidad)
                .filter(n -> n.toLowerCase().contains(texto.toLowerCase()))
                .limit(10)
                .toList();    }


// Solo lectura: el asistente puede consultar posts, nunca crearlos ni borrarlos
@Tool(description = "Devuelve las últimas publicaciones del sitio "
        + "(título, autor, fecha y cantidad de likes)")
public List<String> ultimasPublicaciones(
        @ToolParam(description = "cantidad de publicaciones, entre 1 y 5") int cantidad) {
    int n = Math.max(1, Math.min(cantidad, 5));
    Pageable pageable = PageRequest.of(0, n, Sort.by(Sort.Direction.DESC, "fechaCreacion"));

    return resumir(postService.listarPosts(pageable, null));
}


// Texto corto por post: menos tokens para el modelo
private List<String> resumir(Page<PostResponseDTO> posts) {
    if (posts.isEmpty()) {
        return List.of("No hay publicaciones todavía.");
    }
    return posts.stream()
            .map(p -> "«" + p.titulo() + "» de " + p.nombreUsuario() +
                    " (" + p.fechaCreacion().toLocalDate() + ", " +
                    p.likesCount() + " likes)")
            .toList();
}

}