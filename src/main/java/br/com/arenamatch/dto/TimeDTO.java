package br.com.arenamatch.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class TimeDTO {
    private Long id;

    // Etapa 1
    private String nomeResponsavel;
    private String cpf;
    private String email;
    private String senha;
    private String confirmarSenha;
    private String whatsapp;

    // Etapa 2
    private String nomeTime;
    private String mandoCampo; // VISITANTE ou MANDO
    private BigDecimal taxaJogo;
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String cidade;
    private String uf;
    private String regiao;
    
    // Coordenadas (Preenchidas pelo Backend)
    private Double latitude;
    private Double longitude;

    // Etapa 3
    private List<DisponibilidadeDTO> disponibilidades = new ArrayList<>();
}
