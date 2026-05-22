package br.com.arenamatch.entity;

import br.com.arenamatch.enums.StatusJogo;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "jogo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Jogo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "jogo_seq_gen")
    @SequenceGenerator(name = "jogo_seq_gen", sequenceName = "jogo_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "time_mandante_id", nullable = false)
    private Time timeMandante;

    @ManyToOne
    @JoinColumn(name = "time_visitante_id", nullable = false)
    private Time timeVisitante;

    @ManyToOne
    @JoinColumn(name = "time_solicitante_id", nullable = false)
    private Time timeSolicitante;

    @Column(name = "data_jogo", nullable = false)
    private LocalDate dataJogo;

    @Column(name = "hora_inicio", nullable = false)
    private String horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private String horaFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusJogo status;
}
