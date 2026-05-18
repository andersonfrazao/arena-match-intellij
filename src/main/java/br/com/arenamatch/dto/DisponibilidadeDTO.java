package br.com.arenamatch.dto;

import br.com.arenamatch.enums.Categoria;
import lombok.Data;

@Data
public class DisponibilidadeDTO {
    private String diaSemana; // Seg, Ter...
    private String horaInicio;
    private String horaFim;
    private Categoria categoria;
}
