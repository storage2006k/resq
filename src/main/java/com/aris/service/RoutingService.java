package com.aris.service;

import com.aris.dto.RouteResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoutingService {

    private final RestTemplate restTemplate;
    private final String osrmBaseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RoutingService(RestTemplate restTemplate,
                          @Value("${aris.osrm.base-url}") String osrmBaseUrl) {
        this.restTemplate = restTemplate;
        this.osrmBaseUrl = osrmBaseUrl;
    }

    public RouteResponse calculateRoute(double fromLat, double fromLng, double toLat, double toLng) {
        try {
            String url = String.format("%s/route/v1/driving/%f,%f;%f,%f?overview=full&geometries=geojson",
                    osrmBaseUrl, fromLng, fromLat, toLng, toLat);

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode route = root.path("routes").get(0);

            double distanceMeters = route.path("distance").asDouble();
            double durationSeconds = route.path("duration").asDouble();

            JsonNode coordsNode = route.path("geometry").path("coordinates");
            List<double[]> coordinates = new ArrayList<>();
            for (JsonNode coord : coordsNode) {
                coordinates.add(new double[]{coord.get(1).asDouble(), coord.get(0).asDouble()});
            }

            return new RouteResponse(coordinates, distanceMeters / 1000.0, durationSeconds / 60.0);
        } catch (Exception e) {
            // Fallback: return straight line with estimated ETA
            List<double[]> coords = new ArrayList<>();
            coords.add(new double[]{fromLat, fromLng});
            coords.add(new double[]{toLat, toLng});
            double distKm = haversine(fromLat, fromLng, toLat, toLng);
            return new RouteResponse(coords, distKm, distKm / 0.5); // assume 30 km/h avg
        }
    }

    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
