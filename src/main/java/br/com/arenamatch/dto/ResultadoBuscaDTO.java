package br.com.arenamatch.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResultadoBuscaDTO {
    private Long idTime;
    private String nomeTime;
    private String categoria;
    private String diaSemana;
    private String horario; // Ex: 12:00 - 14:00
    private Double distancia; // Ex: 6.2 (km)
    private String mandoCampo;
    private String ligaVinculada; // Por enquanto pode ser nulo/mock
 // ... campos anteriores
    private LocalDate dataExata;
    private String dataExataFormatada;
    
}