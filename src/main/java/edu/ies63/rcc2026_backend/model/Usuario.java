package edu.ies63.rcc2026_backend.model;

import edu.ies63.rcc2026_backend.enums.Role;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Usuarios")
@Data // getters y setters, toString, equals, etc
@NoArgsConstructor // constructor sin argumentos
@AllArgsConstructor // constructor con argumentos
public class Usuario {
  // id, username, password y role OBLIGATORIOS para UserDetails
  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  private Long id;

  @Column(name = "email", unique = true, nullable = false)
  @Email
  private String username;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(name = "rol", nullable = false)
  private Role role;

  @Column(name = "nombre_completo", nullable = false)
  private String nombre;

  private String biografia;

  private String director;

  @ManyToOne
  @JoinColumn(name = "localidad_id")
  private Localidad localidad;

  @CreationTimestamp
  @Column(name = "fecha_registro", nullable = false, updatable = false)
  private LocalDateTime fechaRegistro;

  // en vez de eliminar la cuenta, estado de cuenta
  private boolean debaja = false;

}
