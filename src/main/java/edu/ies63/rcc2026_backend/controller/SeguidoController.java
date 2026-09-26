package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.SeguidoResponseDTO;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.service.SeguidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/seguidos")
@RequiredArgsConstructor
public class SeguidoController {

  private final SeguidoService seguidoService;

  /*
   * Alterna seguimiento de un grupo (seguir/dejar de seguir).
   * Devuelve si el usuario logueado está siguiendo y la cantidad de seguidores del grupo.
   */
  @PostMapping("/{grupoId}")
  public ResponseEntity<Map<String, Object>> toggleSeguimiento(
      @PathVariable Long grupoId,
      Authentication authentication
  ) {
    Usuario usuario = (Usuario) authentication.getPrincipal();

    boolean siguiendo = seguidoService.toggleSeguimiento(usuario.getId(), grupoId);
    long seguidores = seguidoService.contarSeguidores(grupoId);

    return ResponseEntity.ok(
        Map.of(
            "siguiendo", siguiendo,
            "seguidores", seguidores
        )
    );
  }

  /*
   * Estado de seguimiento de un grupo (si el usuario logueado lo sigue).
   * Devuelve booleano y cantidad de seguidores.
   */
  @GetMapping("/{grupoId}/estado")
  public ResponseEntity<Map<String, Object>> estadoSeguimiento(
      @PathVariable Long grupoId,
      Authentication authentication
  ) {
    Usuario usuario = (Usuario) authentication.getPrincipal();

    boolean siguiendo = seguidoService.estaSiguiendo(usuario.getId(), grupoId);
    long seguidores = seguidoService.contarSeguidores(grupoId);

    return ResponseEntity.ok(
        Map.of(
            "siguiendo", siguiendo,
            "seguidores", seguidores
        )
    );
  }

  /*
   * Grupos a los que sigue el usuario autenticado.
   */
  @GetMapping("/mios")
  public ResponseEntity<List<SeguidoResponseDTO>> listarMisSeguidos(Authentication authentication) {
    Usuario usuario = (Usuario) authentication.getPrincipal();
    return ResponseEntity.ok(seguidoService.listarSeguidos(usuario.getId()));
  }

  /*
   * Grupos a los que sigue un usuario cualquiera (para ver perfil ajeno).
   */
  @GetMapping("/{usuarioId}/seguidos")
  public ResponseEntity<List<SeguidoResponseDTO>> listarSeguidosDeOtroUsuario(@PathVariable Long usuarioId) {
    return ResponseEntity.ok(seguidoService.listarSeguidos(usuarioId));
  }

  /*
   * Usuarios que siguen al grupo indicado.
   */
  @GetMapping("/{grupoId}/seguidores")
  public ResponseEntity<List<SeguidoResponseDTO>> listarSeguidores(@PathVariable Long grupoId) {
    return ResponseEntity.ok(seguidoService.listarSeguidores(grupoId));
  }

  /*
   * Contar cuántos grupos sigue el usuario autenticado.
   */
  @GetMapping("/mios/count")
  public ResponseEntity<Map<String, Object>> contarMisSeguidos(Authentication authentication) {
    Usuario usuario = (Usuario) authentication.getPrincipal();
    long seguidos = seguidoService.contarSeguidos(usuario.getId());
    return ResponseEntity.ok(Map.of("seguidos", seguidos));
  }

  /*
   * Contar cuántos seguidores tiene un grupo.
   */
  @GetMapping("/{grupoId}/count")
  public ResponseEntity<Map<String, Object>> contarSeguidores(@PathVariable Long grupoId) {
    long seguidores = seguidoService.contarSeguidores(grupoId);
    return ResponseEntity.ok(Map.of("seguidores", seguidores));
  }
}
