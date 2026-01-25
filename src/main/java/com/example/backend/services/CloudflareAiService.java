package com.example.backend.services;

import com.example.backend.dtos.CloudflareAiRequestDto;
import com.example.backend.dtos.CloudflareAiResponseDto;
import com.example.backend.exceptions.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class CloudflareAiService {
    
    private final RestTemplate restTemplate;
    private final String accountId;
    private final String apiToken;
    private final String model;
    private final String baseUrl;
    
    public CloudflareAiService(
            RestTemplate restTemplate,
            @Value("${cloudflare.ai.account-id}") String accountId,
            @Value("${cloudflare.ai.api-token}") String apiToken,
            @Value("${cloudflare.ai.model}") String model,
            @Value("${cloudflare.ai.base-url}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.accountId = accountId;
        this.apiToken = apiToken;
        this.model = model;
        this.baseUrl = baseUrl;
    }
    
    /**
     * Generate a response from Cloudflare AI based on the user's prompt
     * 
     * @param prompt The user's message/question
     * @return The AI-generated response
     */
    public String generateResponse(String prompt) {
        return generateResponse(prompt, null, null, null);
    }
    
    /**
     * Generate a response from Cloudflare AI with custom parameters
     * 
     * @param prompt The user's message/question
     * @param maxTokens Maximum number of tokens to generate (optional)
     * @param temperature Controls randomness (0.0 to 1.0, optional)
     * @param topP Nucleus sampling parameter (optional)
     * @return The AI-generated response
     */
    public String generateResponse(String prompt, Integer maxTokens, Double temperature, Double topP) {
        try {
            // Build the request URL
            String url = String.format("%s/accounts/%s/ai/run/%s", baseUrl, accountId, model);
            
            // Create request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiToken);
            
            // Create request body
            CloudflareAiRequestDto requestDto = new CloudflareAiRequestDto();
            requestDto.setPrompt(prompt);
            requestDto.setMaxTokens(maxTokens);
            requestDto.setTemperature(temperature);
            requestDto.setTopP(topP);
            
            HttpEntity<CloudflareAiRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);
            
            // Make the API call
            log.info("=== Cloudflare AI Request ===");
            log.info("URL: {}", url);
            log.info("Model: {}", model);
            log.info("Prompt: {}", prompt);
            if (maxTokens != null) log.info("Max Tokens: {}", maxTokens);
            if (temperature != null) log.info("Temperature: {}", temperature);
            if (topP != null) log.info("Top P: {}", topP);
            
            ResponseEntity<CloudflareAiResponseDto> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    CloudflareAiResponseDto.class
            );
            
            // Extract and return the response
            CloudflareAiResponseDto response = responseEntity.getBody();
            
            if (response == null) {
                log.error("Received null response from Cloudflare AI");
                throw new AppException("Failed to get response from AI service", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
            if (!Boolean.TRUE.equals(response.getSuccess())) {
                log.error("Cloudflare AI request failed. Errors: {}", (Object) response.getErrors());
                throw new AppException("AI service returned an error", HttpStatus.BAD_GATEWAY);
            }
            
            if (response.getResult() == null || response.getResult().getResponse() == null) {
                log.error("Cloudflare AI response missing result data");
                throw new AppException("Invalid response from AI service", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
            String aiResponse = response.getResult().getResponse();
            
            log.info("=== Cloudflare AI Response ===");
            log.info("Success: {}", response.getSuccess());
            log.info("Response: {}", aiResponse);
            if (response.getResult().getUsage() != null) {
                log.info("Token Usage - Prompt: {}, Completion: {}, Total: {}", 
                    response.getResult().getUsage().getPromptTokens(),
                    response.getResult().getUsage().getCompletionTokens(),
                    response.getResult().getUsage().getTotalTokens()
                );
            }
            log.info("=============================");
            
            return aiResponse;
            
        } catch (HttpClientErrorException e) {
            log.error("Client error calling Cloudflare AI: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AppException("Failed to communicate with AI service: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (HttpServerErrorException e) {
            log.error("Server error from Cloudflare AI: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AppException("AI service is temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            log.error("Unexpected error calling Cloudflare AI", e);
            throw new AppException("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
