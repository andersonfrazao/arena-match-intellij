package br.com.arenamatch.jsf.client;

import br.com.arenamatch.dto.BuscaFiltroDTO;
import br.com.arenamatch.dto.ResultadoBuscaDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class MatchClient {
    private final RestClient restClient;

    public MatchClient(RestClient restClient) { this.restClient = restClient; }

    public List<ResultadoBuscaDTO> buscar(Long idTime, BuscaFiltroDTO filtro) {
        return restClient.post()
                .uri("/api/match/buscar/" + idTime)
                .body(filtro)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ResultadoBuscaDTO>>() {});
    }
}