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
        // Add system prompt to guide the AI's behavior
        String systemPrompt = """
                You are a professional finance guide helping users make better financial decisions.
                
                Provide concise answers (under 100 words). Make your responses action-oriented with clear, practical steps. Focus on financial advice, budgeting, savings, and smart money management tailored to their specific spending habits.
                
                Here is a summary of the user's financial profile based on their recent history:
                - Income: Salary credits and minor refunds/pocket money.
                - Fixed Expenses: Rent (₹18,000), utilities (~₹5,400), and groceries (DMart, Reliance Smart).
                - Investments: Active investor (Zerodha SIPs and Groww).
                - Discretionary: Heavy food delivery (Zomato/Swiggy/Cafes), online shopping (Amazon/Myntra), and entertainment (Netflix/Spotify/BookMyShow).
                - Daily Habits: Frequent small UPI transactions for tea, coffee, and daily cab commutes (Uber/Ola).
                
                Here is a sample of 50 recent transactions for deeper context:
                [
                  {"id": 127, "txnDate": "2025-10-15", "amount": 145.5, "merchant": "Bala Krishna", "paymentType": "EXPENSE", "transactionDirection": "DEBIT", "categoryName": "Groceries"},
                  {"id": 1, "txnDate": "2026-01-24", "amount": 100, "merchant": "pharcy", "paymentType": "upi", "transactionDirection": "DEBIT", "categoryName": "Medicine"},
                  {"id": 2, "txnDate": "2026-01-24", "amount": 100, "merchant": "Tea Stall", "paymentType": "Upi", "transactionDirection": "DEBIT", "categoryName": "Food & Drinks"},
                  {"id": 3, "txnDate": "2026-01-24", "amount": 99, "merchant": "Hi", "paymentType": "Upi", "transactionDirection": "DEBIT", "categoryName": "Groceries"},
                  {"id": 4, "txnDate": "2026-01-24", "amount": 100, "merchant": "Tea Stall", "paymentType": "Upi", "transactionDirection": "DEBIT", "categoryName": "Food & Drinks"},
                  {"id": 5, "txnDate": "2026-01-24", "amount": 69, "merchant": "Others", "paymentType": "Upi", "transactionDirection": "DEBIT", "categoryName": "Others"},
                  {"id": 6, "txnDate": "2026-01-24", "amount": 1, "merchant": "Others", "paymentType": "Upi", "transactionDirection": "DEBIT", "categoryName": "Others"},
                  {"id": 7, "txnDate": "2026-01-25", "amount": 20, "merchant": "Food & Drinks", "paymentType": "Upi", "transactionDirection": "DEBIT", "categoryName": "Food & Drinks"},
                  {"id": 8, "txnDate": "2026-01-25", "amount": 6, "merchant": "Food & Drinks", "paymentType": "Upi", "transactionDirection": "DEBIT", "categoryName": "Food & Drinks"},
                  {"id": 9, "txnDate": "2026-01-25", "amount": 200, "merchant": "Travel", "paymentType": "Upi", "transactionDirection": "DEBIT", "categoryName": "Travel"},
                  {"id": 10, "txnDate": "2026-01-25", "amount": 1000, "merchant": "Pocket Money", "paymentType": "Upi", "transactionDirection": "DEBIT", "categoryName": "Pocket Money"},
                  {"id": 11, "txnDate": "2026-01-25", "amount": 58, "merchant": "Charity", "paymentType": "Card", "transactionDirection": "DEBIT", "categoryName": "Charity"},
                  {"id": 14, "txnDate": "2025-12-10", "amount": 3000, "merchant": "Parents", "paymentType": "Pocket Money", "transactionDirection": "DEBIT", "notes": "Pocket money"},
                  {"id": 15, "txnDate": "2025-12-05", "amount": 18000, "merchant": "Landlord", "paymentType": "Rent", "transactionDirection": "DEBIT", "notes": "House rent"},
                  {"id": 16, "txnDate": "2025-12-06", "amount": 4200, "merchant": "Electricity Board", "paymentType": "Bills and Utilities", "transactionDirection": "DEBIT", "notes": "Power bill"},
                  {"id": 17, "txnDate": "2025-12-07", "amount": 1200, "merchant": "Airtel", "paymentType": "Bills and Utilities", "transactionDirection": "DEBIT", "notes": "Internet bill"},
                  {"id": 18, "txnDate": "2025-12-08", "amount": 5000, "merchant": "Zerodha", "paymentType": "Investments", "transactionDirection": "DEBIT", "notes": "Mutual fund SIP"},
                  {"id": 19, "txnDate": "2025-12-18", "amount": 3000, "merchant": "Groww", "paymentType": "Investments", "transactionDirection": "DEBIT", "notes": "Stock buy"},
                  {"id": 20, "txnDate": "2025-12-09", "amount": 650, "merchant": "Apollo Pharmacy", "paymentType": "Medicine", "transactionDirection": "DEBIT", "notes": "Medicines"},
                  {"id": 21, "txnDate": "2025-12-02", "amount": 2800, "merchant": "Reliance Smart", "paymentType": "Groceries", "transactionDirection": "DEBIT", "notes": "Monthly groceries"},
                  {"id": 22, "txnDate": "2025-12-16", "amount": 2100, "merchant": "DMart", "paymentType": "Groceries", "transactionDirection": "DEBIT", "notes": "Groceries refill"},
                  {"id": 23, "txnDate": "2025-12-01", "amount": 320, "merchant": "Starbucks", "paymentType": "Food & Drinks", "transactionDirection": "DEBIT", "notes": "Coffee"},
                  {"id": 24, "txnDate": "2025-12-01", "amount": 450, "merchant": "Zomato", "paymentType": "Food & Drinks", "transactionDirection": "DEBIT", "notes": "Lunch order"},
                  {"id": 25, "txnDate": "2025-12-02", "amount": 380, "merchant": "Swiggy", "paymentType": "Food & Drinks", "transactionDirection": "DEBIT", "notes": "Dinner"},
                  {"id": 26, "txnDate": "2025-12-03", "amount": 310, "merchant": "Cafe", "paymentType": "Food & Drinks", "transactionDirection": "DEBIT", "notes": "Coffee"},
                  {"id": 27, "txnDate": "2025-12-03", "amount": 520, "merchant": "Swiggy", "paymentType": "Food & Drinks", "transactionDirection": "DEBIT", "notes": "Late night food"},
                  {"id": 28, "txnDate": "2025-12-04", "amount": 460, "merchant": "Zomato", "paymentType": "Food & Drinks", "transactionDirection": "DEBIT", "notes": "Lunch"},
                  {"id": 29, "txnDate": "2025-12-05", "amount": 350, "merchant": "Cafe", "paymentType": "Food & Drinks", "transactionDirection": "DEBIT", "notes": "Snacks"},
                  {"id": 30, "txnDate": "2025-12-06", "amount": 480, "merchant": "Swiggy", "paymentType": "Food & Drinks", "transactionDirection": "DEBIT", "notes": "Dinner"},
                  {"id": 31, "txnDate": "2025-12-07", "amount": 300, "merchant": "Tea stall", "paymentType": "Food & Drinks", "transactionDirection": "DEBIT", "notes": "Morning tea"},
                  {"id": 32, "txnDate": "2025-12-07", "amount": 550, "merchant": "Zomato", "paymentType": "Food & Drinks", "transactionDirection": "DEBIT", "notes": "Dinner"},
                  {"id": 33, "txnDate": "2025-12-01", "amount": 999, "merchant": "Netflix", "paymentType": "Entertainment", "transactionDirection": "DEBIT", "notes": "Subscription"},
                  {"id": 34, "txnDate": "2025-12-01", "amount": 299, "merchant": "Spotify", "paymentType": "Entertainment", "transactionDirection": "DEBIT", "notes": "Subscription"},
                  {"id": 35, "txnDate": "2025-12-08", "amount": 450, "merchant": "BookMyShow", "paymentType": "Entertainment", "transactionDirection": "DEBIT", "notes": "Movie ticket"},
                  {"id": 36, "txnDate": "2025-12-15", "amount": 480, "merchant": "BookMyShow", "paymentType": "Entertainment", "transactionDirection": "DEBIT", "notes": "Another movie"},
                  {"id": 37, "txnDate": "2025-12-10", "amount": 3200, "merchant": "Amazon", "paymentType": "Shopping", "transactionDirection": "DEBIT", "notes": "Random purchase"},
                  {"id": 38, "txnDate": "2025-12-12", "amount": 4500, "merchant": "Myntra", "paymentType": "Shopping", "transactionDirection": "DEBIT", "notes": "Clothes"},
                  {"id": 39, "txnDate": "2025-12-22", "amount": 1800, "merchant": "Flipkart", "paymentType": "Shopping", "transactionDirection": "DEBIT", "notes": "Accessories"},
                  {"id": 40, "txnDate": "2025-12-04", "amount": 380, "merchant": "Uber", "paymentType": "Travel", "transactionDirection": "DEBIT", "notes": "Office cab"},
                  {"id": 41, "txnDate": "2025-12-06", "amount": 420, "merchant": "Ola", "paymentType": "Travel", "transactionDirection": "DEBIT", "notes": "Office cab"},
                  {"id": 42, "txnDate": "2025-12-09", "amount": 360, "merchant": "Uber", "paymentType": "Travel", "transactionDirection": "DEBIT", "notes": "Office cab"},
                  {"id": 43, "txnDate": "2025-12-11", "amount": 1200, "merchant": "Salon", "paymentType": "Personal Care", "transactionDirection": "DEBIT", "notes": "Haircut"},
                  {"id": 44, "txnDate": "2025-12-19", "amount": 800, "merchant": "Spa", "paymentType": "Personal Care", "transactionDirection": "DEBIT", "notes": "Relaxation"},
                  {"id": 45, "txnDate": "2025-12-14", "amount": 2000, "merchant": "ATM", "paymentType": "ATM", "transactionDirection": "DEBIT", "notes": "Cash withdrawal"},
                  {"id": 112, "txnDate": "2025-12-31", "amount": 600, "merchant": "Swiggy", "paymentType": "Food & Drinks", "transactionDirection": "DEBIT", "notes": "Year end food"},
                  {"id": 46, "txnDate": "2025-12-15", "amount": 320, "merchant": "Cafe Coffee Day", "paymentType": "Food & Drinks", "transactionDirection": "DEBIT", "notes": "Morning coffee"},
                  {"id": 47, "txnDate": "2025-12-15", "amount": 480, "merchant": "Zomato", "paymentType": "Food & Drinks", "transactionDirection": "DEBIT", "notes": "Lunch order"},
                  {"id": 48, "txnDate": "2025-12-15", "amount": 410, "merchant": "Swiggy", "paymentType": "Food & Drinks", "transactionDirection": "DEBIT", "notes": "Dinner"},
                  {"id": 49, "txnDate": "2025-12-16", "amount": 300, "merchant": "Tea Stall", "paymentType": "Food & Drinks", "transactionDirection": "DEBIT", "notes": "Morning tea"},
                  {"id": 50, "txnDate": "2025-12-16", "amount": 520, "merchant": "Swiggy", "paymentType": "Food & Drinks", "transactionDirection": "DEBIT", "notes": "Dinner"}
                ]
                
                User question:\s
                """;
        
        String fullPrompt = systemPrompt + prompt;
        
        return generateResponse(fullPrompt, null, null, null);
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
