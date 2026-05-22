package br.com.arenamatch.jsf.client;

import br.com.arenamatch.dto.JogoDTO;
import br.com.arenamatch.enums.StatusJogo;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class JogoClient {

    private final RestClient restClient;

    public JogoClient(RestClient restClient) { this.restClient = restClient; }

    public JogoDTO enviarConvite(JogoDTO dto) {
        return restClient.post()
                .uri("/api/jogos/convite")
                .body(dto)
                .retrieve()
                .body(JogoDTO.class);
    }

    public JogoDTO responderConvite(Long jogoId, StatusJogo status) {
        return restClient.put()
                .uri("/api/jogos/" + jogoId + "/responder?status=" + status)
                .retrieve()
                .body(JogoDTO.class);
    }

    public List<JogoDTO> listarPorTime(Long timeId) {
        return restClient.get()
                .uri("/api/jogos/time/" + timeId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<JogoDTO>>() {});
    }
}