package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DtoTest {

  @Test
  @DisplayName("Cobertura general para Records y DTOs autogenerados")
  void testDtos() {
    // ArtistaDetalleDTO
    ArtistaDetalleDTO artista = new ArtistaDetalleDTO(1L, 2L, "Nombre", "url", EstadoInvitacion.PENDIENTE);
    assertThat(artista.invitacionId()).isEqualTo(1L);

    // EventoCalendarioDTO
    EventoCalendarioDTO eventoCal = new EventoCalendarioDTO(1L, "Ev", LocalDate.now(), LocalTime.now(), "Loc", EstadoEvento.PUBLICADO, 1L, "Org");
    assertThat(eventoCal.id()).isEqualTo(1L);

    // EventoCrearRequestDTO
    EventoCrearRequestDTO evCrear = new EventoCrearRequestDTO("Ev", "Det", LocalDate.now(), LocalTime.now(), 1L, "Dir", List.of(1L));
    assertThat(evCrear.nombre()).isEqualTo("Ev");

    // EventoDetalleResponseDTO
    EventoDetalleResponseDTO evDet = new EventoDetalleResponseDTO(1L, "Ev", "Det", LocalDate.now(), LocalTime.now(), new LocalidadSimpleDTO(1L, "Loc"), "Dir", EstadoEvento.PUBLICADO, new OrganizadorSimpleDTO(1L, "Org", "url"), List.of(), new PermisosEventoDTO(true, true, true, LocalDate.now()));
    assertThat(evDet.id()).isEqualTo(1L);

    // EventoEdicionResponseDTO
    EventoEdicionResponseDTO evEdicion = new EventoEdicionResponseDTO(evDet, new EventoEdicionResponseDTO.NotificacionEdicionResumen(true, 1, List.of()));
    assertThat(evEdicion.evento()).isNotNull();

    // ImagenPerfilResponseDTO
    ImagenPerfilResponseDTO imgPerf = new ImagenPerfilResponseDTO(1L, "url", "alt", TipoImagen.FOTOPERFIL);
    assertThat(imgPerf.id()).isEqualTo(1L);

    // ImagenPostDTO
    ImagenPostDTO imgPost = new ImagenPostDTO(1L, "url");
    assertThat(imgPost.id()).isEqualTo(1L);

    // InvitacionDetalleDTO
    InvitacionDetalleDTO inv = new InvitacionDetalleDTO(1L, 1L, "Ev", "Det", LocalDate.now(), LocalTime.now(), "Dir", "Loc", "Org", EstadoInvitacion.PENDIENTE);
    assertThat(inv.idInvitacion()).isEqualTo(1L);

    // LocalidadMapResponseDTO
    LocalidadMapResponseDTO locMap = new LocalidadMapResponseDTO(1L, "Loc", 1.0, 1.0);
    assertThat(locMap.id()).isEqualTo(1L);

    // LocalidadResponseDTO
    LocalidadResponseDTO locResp = new LocalidadResponseDTO(1L, "Loc");
    assertThat(locResp.id()).isEqualTo(1L);

    // LocalidadSimpleDTO
    LocalidadSimpleDTO locSimp = new LocalidadSimpleDTO(1L, "Loc");
    assertThat(locSimp.id()).isEqualTo(1L);

    // NotificacionResponseDTO
    NotificacionResponseDTO notif = new NotificacionResponseDTO(1L, "Msg", false, LocalDateTime.now(), 1L);
    assertThat(notif.id()).isEqualTo(1L);

    // OrganizadorSimpleDTO
    OrganizadorSimpleDTO orgSimp = new OrganizadorSimpleDTO(1L, "Org", "url");
    assertThat(orgSimp.id()).isEqualTo(1L);

    // PermisosEventoDTO
    PermisosEventoDTO perm = new PermisosEventoDTO(true, true, true, LocalDate.now());
    assertThat(perm.esOrganizador()).isTrue();

    // PostCreateDTO
    PostCreateDTO postCr = new PostCreateDTO("Tit", "Desc", TipoPost.POST, "url", List.of());
    assertThat(postCr.titulo()).isEqualTo("Tit");

    // PostResponseDTO
    PostResponseDTO postResp = new PostResponseDTO(1L, "Tit", "Desc", TipoPost.POST, "url", List.of(), LocalDateTime.now(), 1L, "User", "url", Role.USUARIO, 0, false);
    assertThat(postResp.id()).isEqualTo(1L);

    // RedSocialResponseDTO
    RedSocialResponseDTO red = new RedSocialResponseDTO(1L, Plataforma.INSTAGRAM, "url", "user");
    assertThat(red.id()).isEqualTo(1L);

    // RespuestaInvitacionDTO
    RespuestaInvitacionDTO respInv = new RespuestaInvitacionDTO(true);
    assertThat(respInv.aceptar()).isTrue();

    // SeguidoResponseDTO
    SeguidoResponseDTO seg = new SeguidoResponseDTO(1L, 2L, "User", Role.USUARIO, "url", true);
    assertThat(seg.id()).isEqualTo(1L);
  }
}