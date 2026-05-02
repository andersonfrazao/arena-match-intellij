package br.com.arenamatch.util;

public class GeoUtil {

    // Retorna a distância em Quilômetros usando a Fórmula de Haversine
    public static double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        final int RAIO_TERRA = 6371; // Raio médio da Terra em km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return RAIO_TERRA * c;
    }
}