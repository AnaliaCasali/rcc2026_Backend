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

  @PostMapping("/{grupoId}")
  public ResponseEntity<?> toggleSeguimiento(
      @PathVariable Long grupoId,
      Authentication authentication
  ) {

    Usuario usuario = (Usuario) authentication.getPrincipal();

    boolean siguiendo = seguidoService.toggleSeguimiento(
        usuario.getId(),
        grupoId
    );

    long seguidores =
        seguidoService.contarSeguidores(grupoId);

    return ResponseEntity.ok(
        Map.of(
            "siguiendo", siguiendo,
            "seguidores", seguidores
        )
    );
  }

  @GetMapping("/{grupoId}")
  public ResponseEntity<?> estadoSeguimiento(
      @PathVariable Long grupoId,
      Authentication authentication
  ) {

    Usuario usuario = (Usuario) authentication.getPrincipal();

    boolean siguiendo =
        seguidoService.estaSiguiendo(
            usuario.getId(),
            grupoId
        );

    long seguidores =
        seguidoService.contarSeguidores(grupoId);

    return ResponseEntity.ok(
        Map.of(
            "siguiendo", siguiendo,
            "seguidores", seguidores
        )
    );
  }

  // grupos a los que sigue el usuario autenticado
  @GetMapping("/api/v1/seguidos/mios")
  public ResponseEntity<List<SeguidoResponseDTO>> listarMisSeguidos(
      Authentication authentication
  ) {

    Usuario usuario = (Usuario) authentication.getPrincipal();

    return ResponseEntity.ok(
        seguidoService.listarSeguidos(usuario.getId())
    );
  }

  // usuarios que siguen al grupo indicado
  @GetMapping("/api/v1/seguidos/{grupoId}/seguidores")
  public ResponseEntity<List<SeguidoResponseDTO>> listarSeguidores(
      @PathVariable Long grupoId
  ) {

    return ResponseEntity.ok(
        seguidoService.listarSeguidores(grupoId)
    );
  }
}