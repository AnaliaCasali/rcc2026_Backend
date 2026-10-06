package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.EventoCreateDTO;
import edu.ies63.rcc2026_backend.dto.EventoResponseDTO;
import edu.ies63.rcc2026_backend.dto.EventoUpdateDTO;
import edu.ies63.rcc2026_backend.service.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    @PostMapping
    @PreAuthorize("hasAuthority('ORGANIZADOR') or hasRole('ORGANIZADOR')")
    @Operation(summary = "Registrar un nuevo evento e invitar artistas")
    public ResponseEntity<EventoResponseDTO> crearEvento(
            @Valid @RequestBody EventoCreateDTO request,
            Principal principal) {
        EventoResponseDTO creado = eventoService.crearEvento(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PatchMapping("/{id}/confirmar")
    @Operation(summary = "Un artista o grupo confirma su participación en el evento")
    public ResponseEntity<EventoResponseDTO> confirmarParticipacion(
            @PathVariable Long id,
            Principal principal) {
        EventoResponseDTO actualizado = eventoService.confirmarParticipacion(id, principal.getName());
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping
    @Operation(summary = "Listado público de eventos confirmados/publicados")
    public ResponseEntity<Page<EventoResponseDTO>> listarEventos(
            @PageableDefault(size = 10, sort = "fechaEvento") Pageable pageable) {
        return ResponseEntity.ok(eventoService.listarEventosPublicados(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver detalle de un evento por ID")
    public ResponseEntity<EventoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ORGANIZADOR') or hasRole('ORGANIZADOR')")
    @Operation(summary = "Editar un evento existente (Solo organizador propietario)")
    public ResponseEntity<EventoResponseDTO> editarEvento(
            @PathVariable Long id,
            @Valid @RequestBody EventoUpdateDTO request,
            Principal principal) {
        EventoResponseDTO actualizado = eventoService.editarEvento(id, request, principal.getName());
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/calendario")
    @Operation(summary = "Consultar eventos confirmados organizados por mes y año")
    public ResponseEntity<List<EventoResponseDTO>> listarEventosPorMes(
            @RequestParam int anio,
            @RequestParam int mes) {
        List<EventoResponseDTO> eventos = eventoService.listarEventosPorMesYAno(anio, mes);
        return ResponseEntity.ok(eventos);
    }
}