package br.com.arenamatch.jsf.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import br.com.arenamatch.dto.LoginDTO;
import br.com.arenamatch.entity.Time;

@Component
public class AuthClient {

    private final RestClient restClient;

    public AuthClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public Time login(LoginDTO loginDTO) {
        try {
            return restClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(loginDTO)
                    .retrieve()
                    .body(Time.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            return null;
        } catch (HttpClientErrorException.Forbidden e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
