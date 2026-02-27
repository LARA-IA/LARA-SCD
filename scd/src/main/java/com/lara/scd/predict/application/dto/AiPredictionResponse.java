package com.lara.scd.predict.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class AiPredictionResponse {

    @JsonProperty("predictions")
    private List<Prediction> predictions;

    @Data
    public static class Prediction {
        @JsonProperty("class")
        private String classLower;
        
        @JsonProperty("Class")
        private String classUpper;

        @JsonProperty("Probabilidade")
        private Double probabilidade;
        
        @JsonProperty("multClass")
        private String multClassLower;

        @JsonProperty("MultClass")
        private String multClassUpper;

        @JsonProperty("probabilidadeMultClass")
        private Double probabilidadeMultClassLower;

        @JsonProperty("ProbabilidadeMultClass")
        private Double probabilidadeMultClassUpper;
        
        public String getClassValue() {
            return classUpper != null ? classUpper : classLower;
        }

        public String getMultClassValue() {
            return multClassUpper != null ? multClassUpper : multClassLower;
        }

        public Double getMultClassConfidenceValue() {
            return probabilidadeMultClassUpper != null ? probabilidadeMultClassUpper : probabilidadeMultClassLower;
        }
    }
}
