package edu.ies63.rcc2026_backend.model;

import edu.ies63.rcc2026_backend.enums.Pais;
import edu.ies63.rcc2026_backend.enums.Provincia;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class LocalidadTest {
  @Test
  void testSettersYGetters() {
    Localidad loc = new Localidad();
    loc.setId(1L);
    loc.setLocalidad("Resistencia");
    loc.setProvincia(Provincia.CHACO);
    loc.setPais(Pais.ARGENTINA);
    loc.setLatitud(-27.45);
    loc.setLongitud(-58.98);

    assertThat(loc.getId()).isEqualTo(1L);
    assertThat(loc.getLocalidad()).isEqualTo("Resistencia");
    assertThat(loc.getProvincia()).isEqualTo(Provincia.CHACO);
    assertThat(loc.getPais()).isEqualTo(Pais.ARGENTINA);
    assertThat(loc.getLatitud()).isEqualTo(-27.45);
    assertThat(loc.getLongitud()).isEqualTo(-58.98);
  }
}