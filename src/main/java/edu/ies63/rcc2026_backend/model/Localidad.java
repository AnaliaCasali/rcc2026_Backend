package edu.ies63.rcc2026_backend.model;


import edu.ies63.rcc2026_backend.enums.Pais;
import edu.ies63.rcc2026_backend.enums.Provincia;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Localidades")
@Data // getters y setters, toString, equals, etc
@NoArgsConstructor // constructor sin argumentos
@AllArgsConstructor // constructor con argumentos
public class Localidad {
  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  private Long id;

  private String localidad;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Provincia provincia;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Pais pais;

  private Double latitud;

  private Double longitud;

  /*
  id
  localidad
  pais
  provincia
  latitud
  longitud
   */
}
