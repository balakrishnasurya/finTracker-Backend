package com.example.backend.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CloudflareAiRequestDto {
    
    private String prompt;
    
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    
    private Double temperature;
    
    @JsonProperty("top_p")
    private Double topP;
}
