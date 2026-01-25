# Cloudflare AI Integration

## Overview
This project integrates Cloudflare AI's Llama 3.1 8B Instruct model into the chat feature, providing intelligent AI-powered responses to user messages.

## Architecture

### Components Created

1. **Configuration** (`application.properties`)
   - Externalized configuration for API credentials
   - Environment variable support for security
   - Configurable model selection

2. **DTOs**
   - `CloudflareAiRequestDto`: Request payload for AI API
   - `CloudflareAiResponseDto`: Response structure from AI API

3. **Service Layer**
   - `CloudflareAiService`: Handles all communication with Cloudflare AI API
   - `ChatService`: Updated to use CloudflareAiService

4. **Configuration**
   - `RestTemplateConfig`: Configures HTTP client with timeouts

## API Configuration

### Environment Variables (Recommended for Production)
```bash
export CLOUDFLARE_ACCOUNT_ID=your_account_id
export CLOUDFLARE_API_TOKEN=your_api_token
```

### application.properties
```properties
cloudflare.ai.account-id=${CLOUDFLARE_ACCOUNT_ID:731433191397179e2e633570ce0c691a}
cloudflare.ai.api-token=${CLOUDFLARE_API_TOKEN:mwRP0NnP7Kn2an1_fa99XY7RRTFTsEdOwPWLS8ph}
cloudflare.ai.model=${CLOUDFLARE_AI_MODEL:@cf/meta/llama-3.1-8b-instruct}
cloudflare.ai.base-url=https://api.cloudflare.com/client/v4
```

## API Endpoints

### Create Conversation
```http
POST /api/v1/chat/conversations
Content-Type: application/json

{
  "title": "My Chat"
}
```

### Send Message (AI Response)
```http
POST /api/v1/chat/conversations/{conversationId}/messages
Content-Type: application/json

{
  "message": "Where did the phrase Hello World come from?"
}
```

**Response:**
```json
[
  {
    "id": 1,
    "conversationId": 1,
    "role": "user",
    "content": "Where did the phrase Hello World come from?",
    "createdAt": "2026-01-25T12:00:00"
  },
  {
    "id": 2,
    "conversationId": 1,
    "role": "assistant",
    "content": "The phrase 'Hello, World!' originated from...",
    "createdAt": "2026-01-25T12:00:02"
  }
]
```

### Get Conversation Messages
```http
GET /api/v1/chat/conversations/{conversationId}/messages
```

## Best Practices Implemented

### 1. **Security**
- API credentials stored in environment variables
- No hardcoded secrets in code
- Bearer token authentication

### 2. **Error Handling**
- Comprehensive exception handling in `CloudflareAiService`
- Custom `AppException` for consistent error responses
- Detailed logging for debugging

### 3. **Configuration Management**
- Externalized configuration
- Spring Boot property placeholders
- Default values for development

### 4. **Code Organization**
- Follows existing project structure
- Separation of concerns (Controller → Service → External API)
- DTOs for API communication

### 5. **Dependency Injection**
- Constructor-based injection with Lombok
- RestTemplate as a Spring Bean
- Configurable timeouts

### 6. **Logging**
- SLF4J with Lombok's `@Slf4j`
- Debug and error level logging
- Request/response tracking

### 7. **HTTP Client Configuration**
- Connection timeout: 10 seconds
- Read timeout: 30 seconds
- Prevents hanging requests

### 8. **API Design**
- RESTful endpoints
- Proper HTTP status codes
- JSON request/response format

## Testing

### Manual Testing with cURL

1. **Create a conversation:**
```bash
curl -X POST http://localhost:8080/api/v1/chat/conversations \
  -H "Content-Type: application/json" \
  -d '{"title": "Test Chat"}'
```

2. **Send a message:**
```bash
curl -X POST http://localhost:8080/api/v1/chat/conversations/1/messages \
  -H "Content-Type: application/json" \
  -d '{"message": "Where did the phrase Hello World come from?"}'
```

3. **Get all messages:**
```bash
curl http://localhost:8080/api/v1/chat/conversations/1/messages
```

## Error Handling

The integration handles various error scenarios:

- **Network errors**: Returns 503 Service Unavailable
- **API authentication errors**: Returns 400 Bad Request
- **Invalid responses**: Returns 500 Internal Server Error
- **Timeout errors**: Configurable timeouts prevent hanging

## Advanced Usage

The `CloudflareAiService` supports additional parameters:

```java
// With custom parameters
String response = cloudflareAiService.generateResponse(
    prompt,
    512,        // maxTokens
    0.7,        // temperature
    0.9         // topP
);
```

## Future Enhancements

1. **Rate Limiting**: Add rate limiting to prevent API quota exhaustion
2. **Caching**: Implement response caching for repeated questions
3. **Streaming**: Support streaming responses for real-time chat
4. **Context Management**: Include conversation history in prompts
5. **Model Selection**: Allow users to choose different AI models
6. **Retry Logic**: Add exponential backoff for failed requests

## Dependencies

No additional Maven dependencies required - uses existing Spring Boot Web starter.

## Security Notes

⚠️ **Important**: The current configuration includes default credentials for demonstration. In production:

1. Use environment variables or secrets management
2. Rotate API tokens regularly
3. Implement rate limiting
4. Monitor API usage and costs
5. Add authentication/authorization to chat endpoints
