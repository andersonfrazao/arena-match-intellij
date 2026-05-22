package br.com.arenamatch.jsf.client;

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

    public TimeDTO buscarPorId(Long id) {
        return restClient.get().uri("/api/times/" + id).retrieve().body(TimeDTO.class);
    }

    public Time atualizarTime(Long id, TimeDTO dto) {
        return restClient.put().uri("/api/times/" + id).body(dto).retrieve().body(Time.class);
    }

    public Time desativarTime(Long id) {
        return restClient.patch().uri("/api/times/" + id + "/desativar").retrieve().body(Time.class);
    }
}
