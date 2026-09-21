package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.GrupoCreateDTO;
import edu.ies63.rcc2026_backend.dto.GrupoResponseDTO;
import edu.ies63.rcc2026_backend.dto.OrganizadorCreateDTO;
import edu.ies63.rcc2026_backend.dto.OrganizadorResponseDTO;
import edu.ies63.rcc2026_backend.dto.UsuarioCreateDTO;
import edu.ies63.rcc2026_backend.dto.UsuarioResponseDTO;
import edu.ies63.rcc2026_backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth/usuarios")
public class UsuarioController {

  // inyeccion de service de usuario
  private final UsuarioService usuarioService;

  public UsuarioController(UsuarioService usuarioService) {
    this.usuarioService = usuarioService;
  }

  @GetMapping("/lista-usuarios")
  List<UsuarioResponseDTO> listaUsuarios() {
    return usuarioService.listarUsuarios();
  }

  @PostMapping("/registrar-usuario")
  ResponseEntity<UsuarioResponseDTO> registrarUsuario(@Valid @RequestBody
                                                      UsuarioCreateDTO usuario,
                                                      UriComponentsBuilder uriBuilder) {
    UsuarioResponseDTO usuarioCreado = usuarioService.crearUsuario(usuario);

    URI uri = uriBuilder
        .path("/api/v1/auth/usuarios/{id}")
        .buildAndExpand(usuarioCreado.id())
        .toUri();

    return ResponseEntity.created(uri).body(usuarioCreado);
  }

  // Endpoint para Organizador
  @PostMapping("/registrar-organizador")
  public ResponseEntity<OrganizadorResponseDTO> registrarOrganizador(@Valid @RequestBody
                                                                       OrganizadorCreateDTO organizador,
                                                                     UriComponentsBuilder uriBuilder) {
    OrganizadorResponseDTO orgCreado = usuarioService.crearOrganizador(organizador);

    URI uri = uriBuilder
        .path("/api/v1/auth/usuarios/{id}")
        .buildAndExpand(orgCreado.id())
        .toUri();

    return ResponseEntity.created(uri).body(orgCreado);
  }

  // Endpoint para Grupo
  @PostMapping("/registrar-grupo")
  public ResponseEntity<GrupoResponseDTO> registrarGrupo(@Valid @RequestBody
                                                           GrupoCreateDTO grupo,
                                                         UriComponentsBuilder uriBuilder) {
    GrupoResponseDTO grupoCreado = usuarioService.crearGrupo(grupo);

    URI uri = uriBuilder
        .path("/api/v1/auth/usuarios/{id}")
        .buildAndExpand(grupoCreado.id())
        .toUri();

    return ResponseEntity.created(uri).body(grupoCreado);
  }
}
