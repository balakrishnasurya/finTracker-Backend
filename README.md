# Finance Tracker Backend

A comprehensive backend service for a personal and group finance tracking application built with Spring Boot. It offers a wide range of features to manage personal transactions, split expenses with friends (Splitwise clone), track fitness and habit streaks, integrate with Cloudflare AI for intelligent chat, and process SMS notifications.

## 🚀 Key Features

### 💰 Personal Finance Management
* **Transactions:** Record, update, delete, and list income and expenses.
* **Categories:** Organize transactions by customizable categories.
* **Alerts:** Set spending alerts and notifications.

### 👥 Group Expenses & Bill Splitting (Splitwise-like)
* **Groups:** Create groups and add members.
* **Expense Sharing:** Add group transactions split equally, by exact amounts, or custom percentages.
* **Debt Simplification:** Calculate simplified balances and settlements between group members to minimize the number of payments required.

### 🤖 AI-Powered Financial Chat
* **Cloudflare AI Integration:** Intelligent chatbot interface utilizing the powerful `llama-2-7b-chat-int8` model (via Cloudflare Workers AI) to provide financial advice and answer queries.
* **Conversations:** Maintain chat history and multiple conversation contexts.

### 📱 Automated Input
* **SMS Integration:** Automatically parse and process SMS messages from banks to create transaction records instantly without manual data entry.

### 📈 Habit & Fitness Tracking
* **Gym Records:** Keep track of workout logs.
* **Streaks:** Gamified feature to monitor and maintain daily habits or fitness streaks.

## 🛠️ Tech Stack
* **Java** 17+
* **Framework:** Spring Boot (Web, Data JPA, Validation)
* **Database:** Relational Database (SQL)
* **Testing:** JUnit, Mockito, Spring Boot Test
* **AI:** Cloudflare Workers AI API
* **Mapper:** MapStruct

## 📦 Project Structure
* `com.example.backend.controllers`: REST APIs handling incoming HTTP requests.
* `com.example.backend.services`: Core business logic (finance, ML/AI logic, group debt resolution).
* `com.example.backend.repositories`: Database access layers (Spring Data JPA).
* `com.example.backend.entities` / `dtos`: Data models and data transfer objects.
* `com.example.backend.mappers`: Entity to DTO conversion.

## 📡 API Endpoints Overview

Below is a brief summary of the available REST endpoints:

### Personal Finance
* `GET    /api/v1/transactions` - List all transactions
* `POST   /api/v1/transactions` - Add a new transaction
* `PUT    /api/v1/transactions/{id}` - Update a transaction
* `DELETE /api/v1/transactions/{id}` - Delete a transaction
* `GET    /api/v1/categories` - List categories
* `POST   /api/v1/categories` - Create a new category
* `PUT    /api/v1/categories/{id}` - Update a category
* `DELETE /api/v1/categories/{id}` - Delete a category
* `POST   /send-alert` - Manually trigger/send financial alerts

### Groups & Bill Splitting (Splitwise clone)
* `POST   /groups` - Create a new group
* `GET    /groups` - List all groups
* `GET    /groups/{id}` - Get group details
* `POST   /groups/{id}/transactions` - Add an expense (transaction) to the group
* `GET    /groups/{id}/transactions` - View all group expenses
* `GET    /groups/{id}/balances` - View member balances (who owes whom)
* `GET    /groups/{id}/settlement` - Get optimized/simplified debt settlement plan
* `DELETE /groups/{id}` - Delete a group

### AI Chatbot
* `POST   /api/v1/chat/conversations` - Start a new AI chat conversation
* `POST   /api/v1/chat/conversations/{id}/messages` - Send a prompt to the AI assistant
* `GET    /api/v1/chat/conversations/{id}/messages` - Retrieve chat history for a conversation

### Automation & Tracking
* `POST   /api/v1/sms` - Submit SMS strings for automatic transaction parsing
* `GET    /api/v1/sms` - List parsed automatic records
* `GET    /api/v1/streak` - View current streak details
* `POST   /api/v1/streak` - Log a daily streak activity
* `GET    /gym/records` - List gym workout records
* `POST   /gym/records` - Add a gym record
* `PUT    /gym/records/{id}` - Update a gym record
* `DELETE /gym/records/{id}` - Delete a gym record

## ⚡ Quickstart

1. **Clone the repository**
2. **Setup Database**: Execute `database_schema.sql` to initialize tables.
3. **Configure Properties**: Update `src/main/resources/application.properties` with your Database URL, Cloudflare API Token, and Account ID.
4. **Build & Run**:
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```
5. **API Testing**: Use the provided `Splitwise_API.postman_collection.json` or Swagger UI (if configured) to test the endpoints.
