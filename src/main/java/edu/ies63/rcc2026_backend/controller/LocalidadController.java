package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.LocalidadCreateDTO;
import edu.ies63.rcc2026_backend.dto.LocalidadResponseDTO;
import edu.ies63.rcc2026_backend.service.LocalidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth/localidades")
@RequiredArgsConstructor
public class LocalidadController {

  private final LocalidadService localidadService;

  @GetMapping("/lista-localidades")
  public ResponseEntity<List<LocalidadResponseDTO>> obtenerLocalidades() {
    return ResponseEntity.ok(localidadService.listarLocalidades());
  }

  @PostMapping("/agregar-localidad")
  public ResponseEntity<LocalidadResponseDTO> crearLocalidad(@RequestBody LocalidadCreateDTO request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(localidadService.crearLocalidad(request));
  }
}