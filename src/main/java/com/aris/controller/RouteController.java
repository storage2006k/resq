package com.aris.controller;

import com.aris.dto.RouteResponse;
import com.aris.service.NominatimService;
import com.aris.service.RoutingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RouteController {

    private final RoutingService routingService;
    private final NominatimService nominatimService;

    public RouteController(RoutingService routingService, NominatimService nominatimService) {
        this.routingService = routingService;
        this.nominatimService = nominatimService;
    }

    @GetMapping("/route")
    public ResponseEntity<RouteResponse> getRoute(
            @RequestParam String from,
            @RequestParam String to) {
        String[] fromParts = from.split(",");
        String[] toParts = to.split(",");
        double fromLat = Double.parseDouble(fromParts[0]);
        double fromLng = Double.parseDouble(fromParts[1]);
        double toLat = Double.parseDouble(toParts[0]);
        double toLng = Double.parseDouble(toParts[1]);

        return ResponseEntity.ok(routingService.calculateRoute(fromLat, fromLng, toLat, toLng));
    }

    @GetMapping("/geocode")
    public ResponseEntity<List<Map<String, Object>>> geocode(@RequestParam String q) {
        return ResponseEntity.ok(nominatimService.search(q));
    }
}
