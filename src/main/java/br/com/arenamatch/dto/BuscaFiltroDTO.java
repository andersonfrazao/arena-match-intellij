package br.com.arenamatch.dto;

import br.com.arenamatch.enums.Categoria;
import lombok.Data;

@Data
public class BuscaFiltroDTO {
    private String cidade;
    private String diaSemana;
    private Categoria categoria;
    private String horaInicio;
    private String horaFim;
    private Double distanciaKm = 10.0; // Padrão 10km
}
