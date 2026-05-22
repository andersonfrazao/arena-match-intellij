package br.com.arenamatch.dto;

import java.math.BigDecimal;
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
    private String horario;
    private String horaInicio;
    private String horaFim;
    private Double distancia;
    private String mandoCampo;
    private BigDecimal taxaJogo;
    private String enderecoJogo;
    private LocalDate dataExata;
    private String dataExataFormatada;
    private boolean convitePendente;
    private boolean jogoIndisponivel;
}
