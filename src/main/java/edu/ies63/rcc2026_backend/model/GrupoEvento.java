package edu.ies63.rcc2026_backend.model;

import edu.ies63.rcc2026_backend.enums.EstadoInvitacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "gruposxevento", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"evento_id", "perfil_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrupoEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "perfil_id", nullable = false)
    private Usuario artista;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_invitacion", nullable = false, length = 20)
    @Builder.Default
    private EstadoInvitacion estadoInvitacion = EstadoInvitacion.PENDIENTE;

    @Column(name = "confirmado", nullable = false)
    @Builder.Default
    private Boolean confirmado = false;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @PrePersist
    @PreUpdate
    public void sincronizarConfirmado() {
        this.confirmado = EstadoInvitacion.ACEPTADA.equals(this.estadoInvitacion);
    }
}