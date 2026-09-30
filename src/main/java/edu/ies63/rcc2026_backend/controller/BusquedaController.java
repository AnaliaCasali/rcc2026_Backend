package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.PerfilBusquedaDTO;
import edu.ies63.rcc2026_backend.exception.BusquedaInvalidaException;
import edu.ies63.rcc2026_backend.service.BusquedaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/busqueda")
@RequiredArgsConstructor
public class BusquedaController {

    private final BusquedaService busquedaService;

    @GetMapping
    public ResponseEntity<List<PerfilBusquedaDTO>> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String ubicacion,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Integer radioKm) {

        // Si viene lat sin lng, o al revés
        if ((lat != null && lng == null) || (lat == null && lng != null)) {
            throw new BusquedaInvalidaException("lat y lng deben enviarse juntos");
        }

        // Si se envía GPS pero falta radioKm o es negativo
        if (lat != null && (radioKm == null || radioKm < 0)) {
            throw new BusquedaInvalidaException("radioKm debe ser un valor positivo al usar GPS");
        }

        List<PerfilBusquedaDTO> resultados = busquedaService.buscar(texto, ubicacion, lat, lng, radioKm);
        return ResponseEntity.ok(resultados);
    }
}