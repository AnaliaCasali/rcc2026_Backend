package edu.ies63.rcc2026_backend.model;

import edu.ies63.rcc2026_backend.enums.Plataforma;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "redes_sociales")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedSocial {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private Plataforma plataforma;

  @Column(nullable = false)
  private String url;

  @Column(name = "nombre_usuario")
  private String nombreUsuario;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;
}
