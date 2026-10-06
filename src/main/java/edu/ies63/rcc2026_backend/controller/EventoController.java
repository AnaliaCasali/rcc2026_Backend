package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.*;
import edu.ies63.rcc2026_backend.service.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    @PostMapping("/crear")
    @Operation(summary = "Crear evento")
    public ResponseEntity<EventoDetalleResponseDTO> crearEvento(
            @Valid @RequestBody EventoCrearRequestDTO request,
            Principal principal) {
        EventoDetalleResponseDTO response = eventoService.crearEvento(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/editar/{id}")
    @Operation(summary = "Editar evento parcialmente")
    public ResponseEntity<EventoEdicionResponseDTO> editarEvento(
            @PathVariable Long id,
            @Valid @RequestBody EventoEditarRequestDTO request,
            Principal principal) {
        return ResponseEntity.ok(eventoService.editarEvento(id, request, principal.getName()));
    }

    @GetMapping
    @Operation(summary = "Listar eventos por mes para el calendario")
    public ResponseEntity<List<EventoCalendarioDTO>> listarEventosMes(
            @RequestParam int anio,
            @RequestParam int mes,
            Principal principal) {
        return ResponseEntity.ok(eventoService.listarEventosCalendario(anio, mes, principal != null ? principal.getName() : null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle completo del evento")
    public ResponseEntity<EventoDetalleResponseDTO> obtenerPorId(
            @PathVariable Long id,
            Principal principal) {
        return ResponseEntity.ok(eventoService.obtenerDetalleEvento(id, principal != null ? principal.getName() : null));
    }

    @GetMapping("/mis-invitaciones")
    @Operation(summary = "Bandeja de invitaciones para la agrupación")
    public ResponseEntity<List<InvitacionDetalleDTO>> listarMisInvitaciones(Principal principal) {
        return ResponseEntity.ok(eventoService.listarMisInvitaciones(principal.getName()));
    }

    @PatchMapping("/invitaciones/{id}/responder")
    @Operation(summary = "Aceptar o rechazar invitación al evento")
    public ResponseEntity<Map<String, String>> responderInvitacion(
            @PathVariable Long id,
            @Valid @RequestBody RespuestaInvitacionDTO respuesta,
            Principal principal) {
        String mensaje = eventoService.responderInvitacion(id, respuesta.aceptar(), principal.getName());
        return ResponseEntity.ok(Map.of("message", mensaje));
    }
}