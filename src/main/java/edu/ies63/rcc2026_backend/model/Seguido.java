package edu.ies63.rcc2026_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "seguidos",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"usuario_id", "grupo_id"}
        )
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seguido {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Usuario que sigue
  @ManyToOne
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  // Grupo al que sigue
  @ManyToOne
  @JoinColumn(name = "grupo_id", nullable = false)
  private Usuario grupo;
}