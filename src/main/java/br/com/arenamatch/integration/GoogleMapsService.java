package br.com.arenamatch.integration;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Map;
import java.util.List;

@Service
public class GoogleMapsService {
    
    // TODO: Coloque sua chave aqui ou no application.properties
    private final String API_KEY = "AIzaSyDYmOPCVxi4XsiHz29SUgk3zt-A86mYWOA";
    private final RestClient restClient = RestClient.create();

    public double[] getLatLongPorEndereco(String enderecoCompleto, String cep) {
        double[] coordsEndereco = buscarLatLong(enderecoCompleto);
        if (coordsEndereco != null) {
            return coordsEndereco;
        }

        return getLatLongPorCep(cep);
    }

    public double[] getLatLongPorCep(String cep) {
        double[] coordsCep = buscarLatLong(montarBuscaPorCep(cep));
        return coordsCep != null ? coordsCep : new double[]{0.0, 0.0};
    }

    private double[] buscarLatLong(String endereco) {
        if (endereco == null || endereco.isBlank()) {
            return null;
        }

        try {
            Map response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("maps.googleapis.com")
                            .path("/maps/api/geocode/json")
                            .queryParam("address", endereco)
                            .queryParam("key", API_KEY)
                            .build())
                    .retrieve()
                    .body(Map.class);
            
            List results = (List) response.get("results");
            if (results != null && !results.isEmpty()) {
                Map geometry = (Map) ((Map) results.get(0)).get("geometry");
                Map location = (Map) geometry.get("location");
                Number lat = (Number) location.get("lat");
                Number lng = (Number) location.get("lng");
                return new double[]{lat.doubleValue(), lng.doubleValue()};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String montarBuscaPorCep(String cep) {
        if (cep == null || cep.isBlank()) {
            return null;
        }

        return cep.replaceAll("\\D", "") + ", Brazil";
    }
}
