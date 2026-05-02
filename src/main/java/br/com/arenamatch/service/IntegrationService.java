package br.com.arenamatch.service;

import br.com.arenamatch.dto.TimeDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.util.Map;

@Service
public class IntegrationService {

    private final RestClient restClient = RestClient.create();
    // ⚠️ COLOQUE SUA CHAVE AQUI
    private final String GOOGLE_API_KEY = "SUA_CHAVE_GOOGLE_AQUI"; 

    public void preencherEnderecoPorCep(TimeDTO dto) {
        try {
            String url = "https://brasilapi.com.br/api/cep/v1/" + dto.getCep();
            Map resp = restClient.get().uri(url).retrieve().body(Map.class);
            if (resp != null) {
                dto.setLogradouro((String) resp.get("street"));
                dto.setCidade((String) resp.get("city"));
                dto.setUf((String) resp.get("state"));
                dto.setRegiao(getRegiaoPorUF(dto.getUf()));
            }
        } catch (Exception e) {
            System.err.println("Erro BrasilAPI: " + e.getMessage());
        }
    }

    public void preencherLatLong(TimeDTO dto) {
        try {
            String endereco = dto.getLogradouro() + ", " + dto.getNumero() + " - " + dto.getCidade() + ", " + dto.getUf();
            String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" 
                         + endereco.replace(" ", "+") + "&key=" + GOOGLE_API_KEY;

            Map response = restClient.get().uri(url).retrieve().body(Map.class);
            List results = (List) response.get("results");
            
            if (results != null && !results.isEmpty()) {
                Map geometry = (Map) ((Map) results.get(0)).get("geometry");
                Map location = (Map) geometry.get("location");
                dto.setLatitude((Double) location.get("lat"));
                dto.setLongitude((Double) location.get("lng"));
            }
        } catch (Exception e) {
            System.err.println("Erro GoogleMaps: " + e.getMessage());
            // Fallback (ex: centro de SP) se não tiver chave
            dto.setLatitude(-23.550520);
            dto.setLongitude(-46.633308);
        }
    }

    private String getRegiaoPorUF(String uf) {
        if (uf == null) return "";
        if (List.of("SP", "RJ", "MG", "ES").contains(uf)) return "Sudeste";
        if (List.of("PR", "SC", "RS").contains(uf)) return "Sul";
        // ... adicione outros se quiser
        return "Brasil";
    }
}