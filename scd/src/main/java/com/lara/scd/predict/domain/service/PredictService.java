package com.lara.scd.predict.domain.service;

import com.lara.scd.predict.application.dto.AiPredictionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class PredictService {

    private final WebClient webClient;

    public PredictService(WebClient.Builder webClientBuilder, @Value("${app.ai-service.url:http://localhost:8081}") String aiServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(aiServiceUrl).build();
    }

    public AiPredictionResponse predictImage(Resource resource, int idade, String sexo, String localizacao) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        body.add("idade", idade);
        body.add("sexo", sexo);
        body.add("localizacao", localizacao);

        return webClient.post()
                .uri("/predict/")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono(AiPredictionResponse.class)
                .block();
    }
}
