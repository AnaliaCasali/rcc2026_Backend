package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.PerfilResponseDTO;
import edu.ies63.rcc2026_backend.dto.PerfilUpdateDTO;
import edu.ies63.rcc2026_backend.dto.UsuarioResponseDTO;
import edu.ies63.rcc2026_backend.security.dto.CambiarPasswordRequest;
import edu.ies63.rcc2026_backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.ies63.rcc2026_backend.dto.PostResponseDTO;
import edu.ies63.rcc2026_backend.service.PostService;
import org.springframework.data.domain.Sort;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth/usuarios")
public class UsuarioController {

  // inyeccion de service de usuario
  private final UsuarioService usuarioService;
  private final PostService postService;

  public UsuarioController(UsuarioService usuarioService, PostService postService) {
    this.usuarioService = usuarioService;
    this.postService = postService;
  }

  // cambiar size y sort a preferencia de pantalla sugerencias
  @GetMapping("/lista-usuarios")
  public ResponseEntity<Page<UsuarioResponseDTO>> listaUsuarios(
      @PageableDefault(size = 4, sort = "nombre") Pageable pageable) {
    return ResponseEntity.ok(usuarioService.listarUsuarios(pageable));
  }

  @PutMapping(value = "/perfil", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Actualizar el perfil del usuario autenticado (Soporta imágenes)")
  public ResponseEntity<PerfilResponseDTO> actualizarPerfil(
      @Valid @ModelAttribute PerfilUpdateDTO request,
      Principal connectedUser
  ) {
    PerfilResponseDTO response = usuarioService.actualizarPerfil(connectedUser.getName(), request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/perfil/mi-perfil")
  @Operation(summary = "Obtener los datos completos de mi perfil autenticado")
  public ResponseEntity<PerfilResponseDTO> obtenerMiPerfil(Principal connectedUser) {
    return ResponseEntity.ok(usuarioService.obtenerMiPerfil(connectedUser.getName()));
  }

  @GetMapping("/{id}/perfil")
  @Operation(summary = "Ver el perfil público de otro usuario o agrupación")
  public ResponseEntity<PerfilResponseDTO> verPerfilPublico(@PathVariable Long id) {
    return ResponseEntity.ok(usuarioService.verPerfilPublico(id));
  }
  @GetMapping("/{id}/posts")
  @Operation(summary = "Publicaciones de un usuario (perfil), de más reciente a más antigua")
  public ResponseEntity<Page<PostResponseDTO>> postsDeUsuario(
      @PathVariable Long id,
      @PageableDefault(size = 5, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable,
      Principal principal) {
    return ResponseEntity.ok(postService.listarPorUsuario(id, pageable,
        principal != null ? principal.getName() : null));
  }

  @PatchMapping("/cambiar-password")
  @Operation(summary = "Cambiar la contraseña del usuario autenticado")
  public ResponseEntity<Void> cambiarPassword(
          @Valid @RequestBody CambiarPasswordRequest request,
          Principal connectedUser
  ) {
    usuarioService.cambiarPassword(request, connectedUser);
    return ResponseEntity.noContent().build();
  }
}
