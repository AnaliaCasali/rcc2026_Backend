package edu.ies63.rcc2026_backend.model;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "confirmado", nullable = false)
    @Builder.Default
    private Boolean confirmado = false;
}