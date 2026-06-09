package com.lara.scd.predict.domain.service;

import com.lara.scd.predict.application.dto.AiPredictionResponse;
import com.lara.scd.predict.infrastructure.client.PredictFeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PredictService {

    private final PredictFeignClient predictFeignClient;

    public PredictService(PredictFeignClient predictFeignClient) {
        this.predictFeignClient = predictFeignClient;
    }

    public AiPredictionResponse predictImage(MultipartFile file, int idade, String sexo, String localizacao) {
        return predictFeignClient.predict(file, idade, sexo, localizacao);
    }
}
