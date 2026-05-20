package br.com.arenamatch.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "time")
@Data // Lombok: Getters, Setters, ToString, Equals
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Time {

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "time_seq_gen")
    @SequenceGenerator(name = "time_seq_gen", sequenceName = "time_seq", allocationSize = 1)
    private Long id;
    // Dados de Login (Responsável)
    @Column(nullable = false)
    private String nomeResponsavel;

    @Column(nullable = false, length = 14)
    private String cpf;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha; // Armazenar Hash!

    private String whatsapp;

    // Dados do Time
    @Column(nullable = false)
    private String nomeTime;

    @Column(name = "mando_campo")
    private String mandoCampo; // "VISITANTE" ou "MANDO"

    @Column(name = "taxa_jogo", precision = 10, scale = 2)
    private BigDecimal taxaJogo;

    private String escudoUrl;

    // Endereço e Geo
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String cidade;
    private String uf;
    private String regiao;

    private Double latitude;
    private Double longitude;

    // Auditoria
    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;
    
    // ... dentro da classe Time
    @OneToMany(mappedBy = "time", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Disponibilidade> disponibilidades;

    @PrePersist
    public void prePersist() {
        this.dataCadastro = LocalDateTime.now();
    }
}
