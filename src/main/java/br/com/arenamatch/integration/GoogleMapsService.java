package br.com.arenamatch.integration;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Map;
import java.util.List;

@Service
public class GoogleMapsService {
    
    // TODO: Coloque sua chave aqui ou no application.properties
    private final String API_KEY = "AIzaSyAOD3C5_WQJN6nZ0IcSgJTaLVhxft7MOy0"; 
    private final RestClient restClient = RestClient.create();

    public double[] getLatLong(String enderecoCompleto) {
        try {
            // URL do Google Geocoding
            String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" 
                         + enderecoCompleto.replace(" ", "+") 
                         + "&key=" + API_KEY;

            Map response = restClient.get().uri(url).retrieve().body(Map.class);
            
            // Navega no JSON de resposta (simplificado)
            List results = (List) response.get("results");
            if (results != null && !results.isEmpty()) {
                Map geometry = (Map) ((Map) results.get(0)).get("geometry");
                Map location = (Map) geometry.get("location");
                Double lat = (Double) location.get("lat");
                Double lng = (Double) location.get("lng");
                return new double[]{lat, lng};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new double[]{0.0, 0.0}; // Fallback se falhar
    }
}