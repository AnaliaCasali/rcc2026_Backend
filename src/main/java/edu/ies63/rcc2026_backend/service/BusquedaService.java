package edu.ies63.rcc2026_backend.service;

import edu.ies63.rcc2026_backend.dto.PerfilBusquedaDTO;

import java.util.List;

public interface BusquedaService {
    List<PerfilBusquedaDTO> buscar(String texto, String ubicacion, Double lat, Double lng, Integer radioKm, String Rol);
}