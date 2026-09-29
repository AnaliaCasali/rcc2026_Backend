package edu.ies63.rcc2026_backend.model;

import jakarta.persistence.Entity;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "Posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  String titulo;

  // String subtitulo;

  String descripcion;

  LocalDateTime fechaCreacion;

  String tipo; // puede ser enum(?

  String media;
  /*
  id
  idUsuario
  titulo
  subtitulo (indefinido)
  descripcion
  fechacreacion
  tipo (post/premio)
  imagen/video (tabla aparte especificamente para posts/premios)
   */
}
