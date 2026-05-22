package br.com.arenamatch.dto;

import br.com.arenamatch.enums.Categoria;
import lombok.Data;

@Data
public class BuscaFiltroDTO {
    private String diaSemana;
    private Categoria categoria;
    private Double distanciaKm = 10.0;
}
