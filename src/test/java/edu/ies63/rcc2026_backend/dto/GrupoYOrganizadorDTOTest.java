package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.model.Localidad;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrupoYOrganizadorDTOTest {

  @Test
  void testGrupoCreateDTO() {
    GrupoCreateDTO dto1 = new GrupoCreateDTO("grupo@test.com", "pass123", "Grupo Norte", "Bio Grupo", "Director G", 1L);
    GrupoCreateDTO dto2 = new GrupoCreateDTO("grupo@test.com", "pass123", "Grupo Norte", "Bio Grupo", "Director G", 1L);

    // Cobertura de getters
    assertEquals("grupo@test.com", dto1.username());
    assertEquals("pass123", dto1.password());
    assertEquals("Grupo Norte", dto1.nombre());
    assertEquals("Bio Grupo", dto1.biografia());
    assertEquals("Director G", dto1.director());
    assertEquals(1L, dto1.localidadId());

    // Cobertura de métodos heredados del Record
    assertEquals(dto1, dto2);
    assertEquals(dto1.hashCode(), dto2.hashCode());
    assertTrue(dto1.toString().contains("Grupo Norte"));
  }

  @Test
  void testGrupoResponseDTO() {
    Localidad loc = new Localidad();
    loc.setId(1L);
    loc.setLocalidad("Resistencia");

    GrupoResponseDTO dto1 = new GrupoResponseDTO(1L, "grupo@test.com", Role.GRUPO, "Grupo Norte", "Bio Grupo", "Director G", loc);
    GrupoResponseDTO dto2 = new GrupoResponseDTO(1L, "grupo@test.com", Role.GRUPO, "Grupo Norte", "Bio Grupo", "Director G", loc);

    // Cobertura de getters
    assertEquals(1L, dto1.id());
    assertEquals(Role.GRUPO, dto1.role());
    assertEquals("Director G", dto1.director());
    assertNotNull(dto1.localidad());
    assertEquals("Resistencia", dto1.localidad().getLocalidad());

    // Cobertura de métodos heredados del Record
    assertEquals(dto1, dto2);
    assertEquals(dto1.hashCode(), dto2.hashCode());
    assertTrue(dto1.toString().contains("grupo@test.com"));
  }

  @Test
  void testOrganizadorCreateDTO() {
    OrganizadorCreateDTO dto1 = new OrganizadorCreateDTO("org@test.com", "pass123", "Org Sur", "Bio Org", "Director O", 2L);
    OrganizadorCreateDTO dto2 = new OrganizadorCreateDTO("org@test.com", "pass123", "Org Sur", "Bio Org", "Director O", 2L);

    // Cobertura de getters
    assertEquals("org@test.com", dto1.username());
    assertEquals("pass123", dto1.password());
    assertEquals("Org Sur", dto1.nombre());
    assertEquals("Bio Org", dto1.biografia());
    assertEquals("Director O", dto1.director());
    assertEquals(2L, dto1.localidadId());

    // Cobertura de métodos heredados del Record
    assertEquals(dto1, dto2);
    assertEquals(dto1.hashCode(), dto2.hashCode());
    assertTrue(dto1.toString().contains("Org Sur"));
  }

  @Test
  void testOrganizadorResponseDTO() {
    Localidad loc = new Localidad();
    loc.setId(2L);
    loc.setLocalidad("Ushuaia");

    OrganizadorResponseDTO dto1 = new OrganizadorResponseDTO(2L, "org@test.com", Role.ORGANIZADOR, "Org Sur", "Bio Org", "Director O", loc);
    OrganizadorResponseDTO dto2 = new OrganizadorResponseDTO(2L, "org@test.com", Role.ORGANIZADOR, "Org Sur", "Bio Org", "Director O", loc);

    // Cobertura de getters
    assertEquals(2L, dto1.id());
    assertEquals("org@test.com", dto1.username());
    assertEquals(Role.ORGANIZADOR, dto1.role());
    assertNotNull(dto1.localidad());
    assertEquals("Ushuaia", dto1.localidad().getLocalidad());

    // Cobertura de métodos heredados del Record
    assertEquals(dto1, dto2);
    assertEquals(dto1.hashCode(), dto2.hashCode());
    assertTrue(dto1.toString().contains("ORGANIZADOR"));
  }
}