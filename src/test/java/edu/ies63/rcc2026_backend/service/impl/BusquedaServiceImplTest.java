package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.PerfilBusquedaDTO;
import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.model.Localidad;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusquedaServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private BusquedaServiceImpl busquedaService;

    private Usuario balletAuka;
    private Usuario festivalLasToscas;
    private Localidad lasToscas;

    @BeforeEach
    void setUp() {
        lasToscas = new Localidad();
        lasToscas.setId(1L);
        lasToscas.setLocalidad("Las Toscas");
        lasToscas.setLatitud(-28.35);
        lasToscas.setLongitud(-59.26);

        balletAuka = Usuario.builder()
                .id(10L)
                .nombre("Ballet Folklórico Auka")
                .role(Role.GRUPO)
                .biografia("Danzas folklóricas")
                .localidad(lasToscas)
                .debaja(false)
                .build();

        festivalLasToscas = Usuario.builder()
                .id(20L)
                .nombre("Festival Las Toscas")
                .role(Role.ORGANIZADOR)
                .biografia("Organización de eventos")
                .localidad(lasToscas)
                .debaja(false)
                .build();
    }

    @Test
    @DisplayName("Sin parámetros: retorna lista vacía sin consultar el repositorio")
    void buscar_SinParametros_RetornaVacio() {
        List<PerfilBusquedaDTO> resultado = busquedaService.buscar(null, null, null, null, null, null);

        assertThat(resultado).isEmpty();
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("Filtro por texto: filtra por coincidencia parcial insensible a mayúsculas")
    void buscar_PorTexto_FiltraCorrectamente() {
        when(usuarioRepository.findByDebajaFalseAndRoleIn(Set.of(Role.GRUPO, Role.ORGANIZADOR)))
                .thenReturn(List.of(balletAuka, festivalLasToscas));

        List<PerfilBusquedaDTO> resultado = busquedaService.buscar("auka", null, null, null, null, null);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(10L);
        assertThat(resultado.get(0).nombre()).isEqualTo("Ballet Folklórico Auka");
        assertThat(resultado.get(0).distanciaKm()).isNull();
    }

    @Test
    @DisplayName("Filtro por rol GRUPO: consulta únicamente rol GRUPO en el repositorio")
    void buscar_PorRolGrupo_ConsultaSoloGrupos() {
        when(usuarioRepository.findByDebajaFalseAndRoleIn(Set.of(Role.GRUPO)))
                .thenReturn(List.of(balletAuka));

        List<PerfilBusquedaDTO> resultado = busquedaService.buscar(null, null, null, null, null, "GRUPO");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).rol()).isEqualTo("GRUPO");
        verify(usuarioRepository).findByDebajaFalseAndRoleIn(Set.of(Role.GRUPO));
    }

    @Test
    @DisplayName("Filtro por rol inválido: retorna lista vacía inmediatamente")
    void buscar_RolInvalido_RetornaVacio() {
        List<PerfilBusquedaDTO> resultado = busquedaService.buscar(null, null, null, null, null, "ROL_INEXISTENTE");

        assertThat(resultado).isEmpty();
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("Filtro por ubicación: filtra por nombre de localidad")
    void buscar_PorUbicacion_FiltraPorLocalidad() {
        when(usuarioRepository.findByDebajaFalseAndRoleIn(Set.of(Role.GRUPO, Role.ORGANIZADOR)))
                .thenReturn(List.of(balletAuka, festivalLasToscas));

        List<PerfilBusquedaDTO> resultado = busquedaService.buscar(null, "toscas", null, null, null, null);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(PerfilBusquedaDTO::localidad).containsOnly("Las Toscas");
    }

    @Test
    @DisplayName("Filtro GPS: calcula distancia Haversine y filtra por radioKm")
    void buscar_PorGps_CalculaDistanciaYFiltraPorRadio() {
        when(usuarioRepository.findByDebajaFalseAndRoleIn(anySet()))
                .thenReturn(List.of(balletAuka));

        // Mismas coordenadas que Las Toscas (-28.35, -59.26)
        List<PerfilBusquedaDTO> resultado = busquedaService.buscar(null, null, -28.35, -59.26, 10, null);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).distanciaKm()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Filtro GPS fuera de radio: descarta elementos más lejanos")
    void buscar_PorGps_DescartaFueraDeRadio() {
        when(usuarioRepository.findByDebajaFalseAndRoleIn(anySet()))
                .thenReturn(List.of(balletAuka));

        // Coordenadas muy distantes (ej. Ushuaia: -54.80, -68.30) con radio de 50km
        List<PerfilBusquedaDTO> resultado = busquedaService.buscar(null, null, -54.80, -68.30, 50, null);

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Filtro por ubicación: Ignora usuarios que no tienen localidad asignada")
    void buscar_PorUbicacion_IgnoraUsuariosSinLocalidad() {
        Usuario usuarioSinLocalidad = Usuario.builder()
            .id(30L).nombre("Grupo Sin Loc").role(Role.GRUPO).debaja(false).localidad(null).build();

        when(usuarioRepository.findByDebajaFalseAndRoleIn(anySet()))
            .thenReturn(List.of(usuarioSinLocalidad, festivalLasToscas));

        List<PerfilBusquedaDTO> resultado = busquedaService.buscar(null, "toscas", null, null, null, null);

        // Debe ignorar al que tiene localidad null y devolver solo el que coincide
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(20L); // festivalLasToscas
    }
}