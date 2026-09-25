package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.LocalidadMapResponseDTO;
import edu.ies63.rcc2026_backend.dto.LocalidadResponseDTO;
import edu.ies63.rcc2026_backend.service.LocalidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/localidades")
@RequiredArgsConstructor
public class LocalidadController {

  private final LocalidadService localidadService;

  @GetMapping
  public ResponseEntity<List<LocalidadResponseDTO>> listar() {

    return ResponseEntity.ok(
        localidadService.listarLocalidades()
    );
  }

  @GetMapping("/mapa")
  public ResponseEntity<List<LocalidadMapResponseDTO>> listarMapa() {

    return ResponseEntity.ok(
        localidadService.listarLocalidadesMapa()
    );
  }
}
