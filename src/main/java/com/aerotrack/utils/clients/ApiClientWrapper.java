package com.aerotrack.utils.clients;

import com.aerotrack.model.exceptions.ApiRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@Slf4j
public class ApiClientWrapper {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiClientWrapper() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public <T> T sendPostRequest(String endpointUrl, Optional<String> apiKey, Object requestObject, Class<T> responseType) {
        try {
            String requestBody = objectMapper.writeValueAsString(requestObject);
            HttpRequest request = buildRequest(endpointUrl, apiKey, HttpRequest.BodyPublishers.ofString(requestBody), "POST");
            log.info("Sending HTTP POST request: {}", request);

            return handleResponse(httpClient.send(request, HttpResponse.BodyHandlers.ofString()), responseType);
        } catch (JsonProcessingException e) {
            log.error("Error serializing/deserializing JSON: " + e.getMessage());
            throw new ApiRequestException("JSON processing error", e);
        } catch (IOException | InterruptedException e) {
            log.error("Error in API request: " + e.getMessage());
            throw new ApiRequestException("API request error", e);
        }
    }

    public <T> T sendGetRequest(String endpointUrl, Optional<String> apiKey, Class<T> responseType) {
        try {
            HttpRequest request = buildRequest(endpointUrl, apiKey, HttpRequest.BodyPublishers.noBody(), "GET");
            log.info("Sending HTTP GET request: {}", request);

            return handleResponse(httpClient.send(request, HttpResponse.BodyHandlers.ofString()), responseType);
        } catch (IOException | InterruptedException e) {
            log.error("Error in API request: " + e.getMessage());
            throw new ApiRequestException("API request error", e);
        }
    }

    private HttpRequest buildRequest(String endpointUrl, Optional<String> apiKey, HttpRequest.BodyPublisher bodyPublisher, String method) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(endpointUrl))
                .header("Content-Type", "application/json");

        apiKey.ifPresent(s -> requestBuilder.header("x-api-key", s));

        if ("POST".equals(method)) {
            requestBuilder.POST(bodyPublisher);
        } else if ("GET".equals(method)) {
            requestBuilder.GET();
        }

        return requestBuilder.build();
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseType) throws IOException {
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), responseType);
        } else {
            log.error("Failed to get response: Status Code = " + response.statusCode());
            log.debug(response.toString());
            throw new ApiRequestException("Response status code: " + response.statusCode());
        }
    }
}
