package br.com.arenamatch.integration;

import br.com.arenamatch.dto.TimeDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Service
public class BrasilApiService {

    private final RestClient restClient = RestClient.create();

    public void preencherEndereco(TimeDTO dto) {
        try {
            String url = "https://brasilapi.com.br/api/cep/v1/" + dto.getCep();
            Map resp = restClient.get().uri(url).retrieve().body(Map.class);

            if (resp != null) {
                dto.setLogradouro((String) resp.get("street"));
                dto.setCidade((String) resp.get("city"));
                dto.setUf((String) resp.get("state"));
                dto.setRegiao(getRegiaoPorUF(dto.getUf())); // Implementar helper simples
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar CEP: " + e.getMessage());
        }
    }
    
    private String getRegiaoPorUF(String uf) {
        // Lógica simples (Norte, Sul, Sudeste...)
        if("SP".equals(uf) || "RJ".equals(uf) || "MG".equals(uf)) return "Sudeste";
        return "Brasil"; // Simplificado
    }
}