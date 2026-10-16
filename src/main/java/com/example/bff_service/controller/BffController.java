package com.example.bffservice.controller; // Ajusta el paquete si es necesario

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@CrossOrigin(origins = "http://localhost:4200") // Angular ahora apuntará aquí
public class BffController {

    @Value("${pedidos.service.url}")
    private String pedidosServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @RequestMapping(value = "/api/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> reenviarPeticion(HttpServletRequest request, @RequestBody(required = false) String body) {
        
        // 1. Construir la URL hacia el backend en el puerto 8080
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        String urlDestino = pedidosServiceUrl + requestUri + (queryString != null ? "?" + queryString : "");

        // 2. Extraer el token y ponerlo en los headers
        HttpHeaders headers = new HttpHeaders();
        JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            headers.setBearerAuth(auth.getToken().getTokenValue());
        }
        headers.set("Content-Type", request.getContentType() != null ? request.getContentType() : "application/json");

        // 3. Enviar al backend
        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(urlDestino, HttpMethod.valueOf(request.getMethod()), httpEntity, String.class);
    }
}