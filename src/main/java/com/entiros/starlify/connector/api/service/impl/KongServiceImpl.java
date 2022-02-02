package com.entiros.starlify.connector.api.service.impl;

import com.entiros.starlify.connector.api.dto.kong.Consumer;
import com.entiros.starlify.connector.api.dto.kong.KongResponse;
import com.entiros.starlify.connector.api.dto.kong.KongServiceDto;
import com.entiros.starlify.connector.api.dto.kong.Route;
import com.entiros.starlify.connector.api.service.KongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class KongServiceImpl implements KongService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${kong.server.url}")
    private String apiServer;

    @Override
    public KongResponse<KongServiceDto> getServices() {
        HttpHeaders headers = getHttpHeaders();
        ResponseEntity<KongResponse<KongServiceDto>> response = restTemplate.exchange(apiServer + "/services",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<KongResponse<KongServiceDto>>() {
                });

        return response.getBody();
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return headers;
    }

    @Override
    public KongResponse<Route> getServiceRoutes(String serviceId) {
        HttpHeaders headers = getHttpHeaders();
        ResponseEntity<KongResponse<Route>> response = restTemplate.exchange(apiServer + "/services/{serviceId}/routes",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<KongResponse<Route>>() {
                }, serviceId);
        return response.getBody();
    }

    @Override
    public KongResponse<Consumer> getConsumers() {
        HttpHeaders headers = getHttpHeaders();
        ResponseEntity<KongResponse<Consumer>> response = restTemplate.exchange(apiServer + "/consumers",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<KongResponse<Consumer>>() {
                });
        return response.getBody();
    }
}
