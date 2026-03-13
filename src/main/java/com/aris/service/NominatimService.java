package com.aris.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NominatimService {

    private final RestTemplate restTemplate;
    private final String nominatimBaseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NominatimService(RestTemplate restTemplate,
                            @Value("${aris.nominatim.base-url}") String nominatimBaseUrl) {
        this.restTemplate = restTemplate;
        this.nominatimBaseUrl = nominatimBaseUrl;
    }

    public List<Map<String, Object>> search(String query) {
        try {
            String url = String.format("%s/search?q=%s&format=json&limit=5",
                    nominatimBaseUrl, query.replace(" ", "+"));

            String response = restTemplate.getForObject(url, String.class);
            JsonNode results = objectMapper.readTree(response);

            List<Map<String, Object>> searchResults = new ArrayList<>();
            for (JsonNode result : results) {
                Map<String, Object> item = new HashMap<>();
                item.put("displayName", result.path("display_name").asText());
                item.put("lat", result.path("lat").asDouble());
                item.put("lng", result.path("lon").asDouble());
                searchResults.add(item);
            }
            return searchResults;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
