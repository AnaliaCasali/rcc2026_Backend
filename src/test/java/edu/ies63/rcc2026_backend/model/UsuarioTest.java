package edu.ies63.rcc2026_backend.model;

import edu.ies63.rcc2026_backend.enums.Role;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

  @Test
  void testUsuarioBuilderGettersYSetters() {
    Localidad localidadMock = new Localidad();
    localidadMock.setId(1L);

    Usuario usuario = Usuario.builder()
        .id(1L)
        .username("test@test.com")
        .password("secreta123")
        .role(Role.USUARIO)
        .nombre("Juan Perez")
        .biografia("Bailarín de folklore")
        .director("Director X")
        .localidad(localidadMock)
        .debaja(false)
        .whatsapp("3624123456")
        .imagenesPerfil(new ArrayList<>())
        .redes(new ArrayList<>())
        .build();

    // Validar getters
    assertEquals(1L, usuario.getId());
    assertEquals("test@test.com", usuario.getUsername());
    assertEquals("secreta123", usuario.getPassword());
    assertEquals(Role.USUARIO, usuario.getRole());
    assertEquals("Juan Perez", usuario.getNombre());
    assertEquals("Bailarín de folklore", usuario.getBiografia());
    assertEquals("Director X", usuario.getDirector());
    assertEquals(1L, usuario.getLocalidad().getId());
    assertFalse(usuario.isDebaja());
    assertEquals("3624123456", usuario.getWhatsapp());
    assertNotNull(usuario.getImagenesPerfil());
    assertNotNull(usuario.getRedes());

    // Validar setters
    usuario.setNombre("Pedro Gomez");
    assertEquals("Pedro Gomez", usuario.getNombre());
  }

  @Test
  void testUserDetailsImplementacion() {
    Usuario usuario = new Usuario();

    // La implementación provista en tu código devuelve true por defecto
    assertTrue(usuario.isAccountNonExpired());
    assertTrue(usuario.isAccountNonLocked());
    assertTrue(usuario.isCredentialsNonExpired());
    assertTrue(usuario.isEnabled());

    // La implementación actual de getAuthorities() devuelve una lista vacía
    assertTrue(usuario.getAuthorities().isEmpty());
  }

  @Test
  void testEqualsHashCodeYToString() {
    Usuario u1 = Usuario.builder().id(1L).username("test@test.com").build();
    Usuario u2 = Usuario.builder().id(1L).username("test@test.com").build();
    Usuario u3 = Usuario.builder().id(2L).username("otro@test.com").build();

    assertEquals(u1, u2);
    assertNotEquals(u1, u3);
    assertEquals(u1.hashCode(), u2.hashCode());
    assertNotNull(u1.toString());
  }
}