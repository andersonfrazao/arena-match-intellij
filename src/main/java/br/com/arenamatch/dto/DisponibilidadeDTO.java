package br.com.arenamatch.dto;
import lombok.Data;

@Data
public class DisponibilidadeDTO {
    private String diaSemana; // Seg, Ter...
    private String horaInicio;
    private String horaFim;
    private String categoria; // Veterano, Esporte...
}