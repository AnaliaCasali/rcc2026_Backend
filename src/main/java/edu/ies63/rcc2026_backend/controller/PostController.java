package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.PostCreateDTO;
import edu.ies63.rcc2026_backend.dto.PostResponseDTO;
import edu.ies63.rcc2026_backend.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;

  // Principal es null cuando entra un visitante sin sesión
  private String nombreONull(Principal principal) {
    return principal != null ? principal.getName() : null;
  }

  @GetMapping
  @Operation(summary = "Listar todas las publicaciones (tipo POST) - público")
  public ResponseEntity<Page<PostResponseDTO>> listarPosts(
      @PageableDefault(size = 10, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable,
      Principal principal) {
    return ResponseEntity.ok(postService.listarPosts(pageable, nombreONull(principal)));
  }

  @GetMapping("/feed")
  @Operation(summary = "Feed: publicaciones de las cuentas que sigo")
  public ResponseEntity<Page<PostResponseDTO>> feed(
      @PageableDefault(size = 20, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable,
      Principal principal) {
    return ResponseEntity.ok(postService.listarFeed(pageable, principal.getName()));
  }

  @GetMapping("/premios")
  @Operation(summary = "Listar todos los premios (tipo PREMIO)")
  public ResponseEntity<Page<PostResponseDTO>> listarPremios(
      @PageableDefault(size = 10, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable,
      Principal principal) {
    return ResponseEntity.ok(postService.listarPremios(pageable, nombreONull(principal)));
  }

  @PostMapping(value = "crear-post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Crear una nueva publicación")
  public ResponseEntity<PostResponseDTO> crearPost(
      @Valid @ModelAttribute PostCreateDTO request,
      Principal connectedUser
  ) {
    PostResponseDTO response = postService.crearPost(request, connectedUser.getName());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // agregar endpoints para editar y eliminar
}