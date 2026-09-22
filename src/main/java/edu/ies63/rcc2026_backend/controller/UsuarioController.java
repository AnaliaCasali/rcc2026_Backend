package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.UsuarioResponseDTO;
import edu.ies63.rcc2026_backend.service.UsuarioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
