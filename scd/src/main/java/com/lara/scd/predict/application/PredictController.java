package com.lara.scd.predict.application;

import com.lara.scd.predict.application.dto.PredictHelloRequestDto;
import com.lara.scd.predict.domain.service.PredictService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/predict")
public class PredictController {

    private final PredictService predictService;

    public PredictController(PredictService predictService) {
        this.predictService = predictService;
    }

    @PostMapping("/hello")
    public String helloWorld(@RequestBody PredictHelloRequestDto dto) {
        predictService.sendMessage(dto);
        return "Hello " + dto.name() + " " + dto.surname() + ". Mensagem enviada para fila.";
    }

    @PostMapping(value = "/classify", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public String classifyImage(@org.springframework.web.bind.annotation.RequestParam("file") MultipartFile file) throws java.io.IOException {
        String base64Image = java.util.Base64.getEncoder().encodeToString(file.getBytes());
        var dto = new com.lara.scd.predict.application.dto.PredictImageRequestDto(base64Image, file.getOriginalFilename());
        predictService.sendImage(dto);
        return "Imagem enviada para classificação.";
    }
}
