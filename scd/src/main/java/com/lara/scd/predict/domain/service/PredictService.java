package com.lara.scd.predict.domain.service;

import com.lara.scd.predict.application.dto.PredictHelloRequestDto;
import com.lara.scd.predict.application.dto.PredictImageRequestDto;
import com.lara.scd.predict.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PredictService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public PredictService(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendMessage(PredictHelloRequestDto dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao converter DTO para JSON", e);
        }
    }

    public void sendImage(PredictImageRequestDto dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao converter DTO para JSON", e);
        }
    }
}
