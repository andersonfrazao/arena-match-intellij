package br.com.arenamatch.client;

import br.com.arenamatch.dto.TimeDTO;
import br.com.arenamatch.entity.Time;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TimeClient {
    private final RestClient restClient;

    public TimeClient(RestClient restClient) { this.restClient = restClient; }

    public TimeDTO buscarEnderecoPorCep(String cep) {
        return restClient.get().uri("/api/times/cep/" + cep).retrieve().body(TimeDTO.class);
    }

    public Time salvarTime(TimeDTO dto) {
        return restClient.post().uri("/api/times").body(dto).retrieve().body(Time.class);
    }
}