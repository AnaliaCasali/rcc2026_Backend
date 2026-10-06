package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.NotificacionResponseDTO;
import edu.ies63.rcc2026_backend.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

  private final NotificacionService notificacionService;

  @GetMapping
  @Operation(summary = "Mis notificaciones, de más reciente a más antigua")
  public ResponseEntity<List<NotificacionResponseDTO>> listarMias(Principal principal) {
    return ResponseEntity.ok(notificacionService.listarMias(principal.getName()));
  }

  @GetMapping("/no-leidas/count")
  @Operation(summary = "Cantidad de notificaciones sin leer")
  public ResponseEntity<Map<String, Long>> contarNoLeidas(Principal principal) {
    return ResponseEntity.ok(Map.of("count", notificacionService.contarNoLeidas(principal.getName())));
  }

  @PatchMapping("/{id}/leer")
  @Operation(summary = "Marcar una notificación como leída")
  public ResponseEntity<Void> marcarLeida(@PathVariable Long id, Principal principal) {
    notificacionService.marcarLeida(id, principal.getName());
    return ResponseEntity.noContent().build();
  }
}