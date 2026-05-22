package br.com.arenamatch.jsf.client;

import br.com.arenamatch.dto.LoginDTO;
import br.com.arenamatch.entity.Time; // Ou UsuarioDTO se preferir criar um DTO de resposta
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class AuthClient {

    private final RestClient restClient;

    public AuthClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public Time login(LoginDTO loginDTO) {
        try {
            return restClient.post()
                    .uri("/api/auth/login") // Atenção: deve bater com o @RequestMapping do Controller
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(loginDTO)
                    .retrieve()
                    .body(Time.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            // Retorna null ou lança uma exceção personalizada para o Bean tratar
            return null; 
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}