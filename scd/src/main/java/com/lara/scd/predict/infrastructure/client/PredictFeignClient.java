package com.lara.scd.predict.infrastructure.client;

import com.lara.scd.predict.application.dto.AiPredictionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "predictFeignClient", url = "${app.ai-service.url:http://api:8081}")
public interface PredictFeignClient {

    @PostMapping(value = "/predict/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    AiPredictionResponse predict(
            @RequestPart("file") MultipartFile file,
            @RequestPart("idade") int idade,
            @RequestPart("sexo") String sexo,
            @RequestPart("localizacao") String localizacao
    );
}
