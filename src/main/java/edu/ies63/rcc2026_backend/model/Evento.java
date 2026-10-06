package edu.ies63.rcc2026_backend.model;

import edu.ies63.rcc2026_backend.enums.EstadoEvento;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "eventos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "perfil_id", nullable = false)
    private Usuario organizador;

    @CreationTimestamp
    @Column(name = "fecha_public", updatable = false)
    private LocalDateTime fechaPublic;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDate fechaEvento;

    @Column(name = "hora_evento", nullable = false)
    private LocalTime horaEvento;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "ubicacion", nullable = false, length = 255)
    private String ubicacion;

    // #idLocalidad
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "localidad_id", nullable = false)
    private Localidad localidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoEvento estado = EstadoEvento.PENDIENTE;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GrupoEvento> grupos = new ArrayList<>();
}