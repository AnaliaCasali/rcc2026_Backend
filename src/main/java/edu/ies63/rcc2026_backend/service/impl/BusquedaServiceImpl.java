package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.PerfilBusquedaDTO;
import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import edu.ies63.rcc2026_backend.service.BusquedaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusquedaServiceImpl implements BusquedaService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public List<PerfilBusquedaDTO> buscar(String texto, String ubicacion, Double lat, Double lng, Integer radioKm) {
        boolean hasTexto = texto != null && !texto.trim().isBlank();
        boolean hasUbicacion = ubicacion != null && !ubicacion.trim().isBlank();
        boolean hasGps = lat != null && lng != null && radioKm != null;

        // Si no hay texto ni ubicación/GPS, se devuelve lista vacía
        if (!hasTexto && !hasUbicacion && !hasGps) {
            return List.of();
        }

        final String textoFiltro = hasTexto ? texto.trim().toLowerCase() : null;
        final String ubicacionFiltro = hasUbicacion ? ubicacion.trim().toLowerCase() : null;

        // Solo usuarios con rol GRUPO u ORGANIZADOR
        List<Usuario> candidatos = usuarioRepository.findByDebajaFalseAndRoleIn(
                Set.of(Role.GRUPO, Role.ORGANIZADOR)
        );

        return candidatos.stream()
                // 1. Filtro por texto en el nombre
                .filter(u -> {
                    if (textoFiltro == null) return true;
                    return u.getNombre() != null && u.getNombre().toLowerCase().contains(textoFiltro);
                })
                // 2. Filtro por ubicación escrita
                .filter(u -> {
                    if (ubicacionFiltro == null) return true;
                    if (u.getLocalidad() == null || u.getLocalidad().getLocalidad() == null) return false;
                    return u.getLocalidad().getLocalidad().toLowerCase().contains(ubicacionFiltro);
                })
                // 3. Mapear calculando distancia Haversine si se pasó GPS
                .map(u -> {
                    Double distancia = null;
                    if (hasGps && u.getLocalidad() != null
                            && u.getLocalidad().getLatitud() != null
                            && u.getLocalidad().getLongitud() != null) {
                        distancia = calcularDistanciaKm(lat, lng, u.getLocalidad().getLatitud(), u.getLocalidad().getLongitud());
                        // Redondear a un decimal
                        distancia = Math.round(distancia * 10.0) / 10.0;
                    }

                    String fotoUrl = (u.getImagenesPerfil() != null && !u.getImagenesPerfil().isEmpty())
                            ? u.getImagenesPerfil().getFirst().getUrl()
                            : null;

                    return new PerfilBusquedaDTO(
                            u.getId(),
                            u.getNombre(),
                            u.getRole().name(),
                            u.getBiografia(),
                            u.getLocalidad() != null ? u.getLocalidad().getLocalidad() : null,
                            fotoUrl,
                            distancia
                    );
                })
                // 4. Filtrar por radioKm si se pasó GPS
                .filter(dto -> {
                    if (!hasGps) return true;
                    return dto.distanciaKm() != null && dto.distanciaKm() <= radioKm;
                })
                // 5. Ordenamiento: primero los que empiezan con el texto, luego por distancia
                .sorted(Comparator.comparing((PerfilBusquedaDTO dto) -> {
                    if (textoFiltro != null && dto.nombre() != null && dto.nombre().toLowerCase().startsWith(textoFiltro)) {
                        return 0; // Prioridad alta
                    }
                    return 1;
                }).thenComparing((PerfilBusquedaDTO dto) -> {
                    return dto.distanciaKm() != null ? dto.distanciaKm() : Double.MAX_VALUE;
                }))
                // 6. Limitar a 20 resultados
                .limit(20)
                .toList();
    }

    private double calcularDistanciaKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}