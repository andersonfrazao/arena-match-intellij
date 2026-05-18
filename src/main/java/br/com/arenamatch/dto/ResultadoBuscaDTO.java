package br.com.arenamatch.dto;

import java.time.LocalDate;

import br.com.arenamatch.enums.Categoria;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResultadoBuscaDTO {
    private Long idTime;
    private String nomeTime;
    private Categoria categoria;
    private String diaSemana;
    private String horario; // Ex: 12:00 - 14:00
    private String horaInicio;
    private String horaFim;
    private Double distancia; // Ex: 6.2 (km)
    private String mandoCampo;
    private LocalDate dataExata;
    private String dataExataFormatada;
    
}
