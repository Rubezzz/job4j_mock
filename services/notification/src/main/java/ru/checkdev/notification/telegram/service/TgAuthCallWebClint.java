package ru.checkdev.notification.telegram.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.PersonDTO;

/**
 * 3. Мидл
 * Класс реализует методы get и post для отправки сообщений через WebClient
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 12.09.2023
 */
@Service
@Slf4j
public class TgAuthCallWebClint {
    private WebClient webClient;
    @Value("${security.oauth2.client.id}")
    private String clientId;
    @Value("${security.oauth2.client.secret}")
    private String clientSecret;
    @Value("${security.oauth2.tokenUri}")
    private String tokenUri;

    public TgAuthCallWebClint(@Value("${server.auth}") String urlAuth) {
        this.webClient = WebClient.create(urlAuth);
    }

    public <T> Mono<T> doGet(String url, Class<T> responseType) {
        return webClient
                .get()
                .uri(url)
                .retrieve()
                .onStatus(
                        status -> status == HttpStatus.NO_CONTENT,
                        response -> Mono.empty()
                )
                .bodyToMono(responseType)
                .doOnError(err -> log.error("API not found: {}", err.getMessage()));
    }

    public <T> Mono<Object> doPost(String url, T requestBody) {
        return webClient
                .post()
                .uri(url)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Object.class)
                .doOnError(err -> log.error("API not found: {}", err.getMessage()));
    }

    public <T> Mono<HttpStatus> doPut(String url, T requestBody, String token) {
        return webClient.put()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchangeToMono(response -> {
                    HttpStatus status = response.statusCode();
                    if (status.is2xxSuccessful()) {
                        return response.releaseBody()
                                .thenReturn(status);
                    }
                    return Mono.just(status);
                })
                .doOnError(err -> log.error("API request failed: {}", err.getMessage()));
    }

    public Mono<String> authorize(String username, String password) {
        var formData = new LinkedMultiValueMap<String, String>();
        formData.add("grant_type", "password");
        formData.add("username", username);
        formData.add("password", password);
        formData.add("scope", "any");

        return webClient.post()
                .uri(tokenUri)
                .headers(headers -> {
                    headers.setBasicAuth(clientId, clientSecret);
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                })
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(jsonNode -> jsonNode.get("access_token").asText())
                .doOnError(e -> log.error("Authorization error: {}", e.getMessage()));
    }

    public void setWebClient(WebClient webClient) {
        this.webClient = webClient;
    }
}
