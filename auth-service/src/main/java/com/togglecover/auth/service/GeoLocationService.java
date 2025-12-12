package com.togglecover.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class GeoLocationService {

    private final RestTemplate restTemplate;

    public GeoLocationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getLocation(String ipAddress) {
        try {
            // Using ip-api.com (free tier)
            String url = "http://ip-api.com/json/" + ipAddress;
            Map response = restTemplate.getForObject(url, Map.class);

            if (response != null && "success".equals(response.get("status"))) {
                String city = (String) response.get("city");
                String country = (String) response.get("country");
                return city + ", " + country;
            }
        } catch (Exception e) {
            log.error("Failed to get location for IP {}: {}", ipAddress, e.getMessage());
        }

        return "Unknown";
    }

    public Map<String, Object> getLocationDetails(String ipAddress) {
        try {
            String url = "http://ip-api.com/json/" + ipAddress;
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.error("Failed to get location details for IP {}: {}", ipAddress, e.getMessage());
            return Map.of("status", "fail");
        }
    }
}