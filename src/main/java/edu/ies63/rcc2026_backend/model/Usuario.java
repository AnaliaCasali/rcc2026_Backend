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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "Usuarios")
@Data // getters y setters, toString, equals, etc
@Builder
@NoArgsConstructor // constructor sin argumentos
@AllArgsConstructor // constructor con argumentos
public class Usuario implements UserDetails {
    // id, username, password y role OBLIGATORIOS para UserDetails
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    // NUEVO: fecha y hora en que aceptó los Términos y Condiciones al registrarse.
    // La toma el servidor (no viene del cliente). Null en cuentas creadas antes de este cambio.
    @Column(name = "fecha_aceptacion_terminos")
    private LocalDateTime fechaAceptacionTerminos;

    // en vez de eliminar la cuenta, estado de cuenta
    private boolean debaja = false;

    @Column(name = "whatsapp", length = 20)
    private String whatsapp;

    // agregar entidad imagen y llamar por id (para editar perfil)
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImagenPerfil> imagenesPerfil;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RedSocial> redes;

    @Column(name = "verificado", nullable = false)
    @Builder.Default
    private Boolean verificado = false;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()), new SimpleGrantedAuthority(role.name()));}

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return this.verificado;
    }

}
