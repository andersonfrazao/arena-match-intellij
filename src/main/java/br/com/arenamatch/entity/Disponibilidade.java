package br.com.arenamatch.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.enums.CategoriaConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "disponibilidade")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Disponibilidade {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "disp_seq_gen")
    @SequenceGenerator(name = "disp_seq_gen", sequenceName = "disponibilidade_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "time_id", nullable = false)
    @JsonIgnore
    private Time time;

    @Column(name = "dia_semana", nullable = false)
    private String diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private String horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private String horaFim;

    @Column(nullable = false)
    @Convert(converter = CategoriaConverter.class)
    private Categoria categoria;
    

    
}
