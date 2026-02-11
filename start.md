# Quick Start Guide

## Prerequisites to Install

### 1. Install Java 17
- Download: [Oracle JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) or [OpenJDK 17](https://adoptium.net/)
- Install and verify:
```bash
java -version
```
(Should show version 17)

### 2. Install PostgreSQL
- Download: [PostgreSQL](https://www.postgresql.org/download/)
- During installation, remember your password
- Default port: 5432

### 3. Install Git (Optional but recommended)
- Download: [Git](https://git-scm.com/downloads)

---

## Database Setup

### Create Database
1. Open pgAdmin or command line
2. Create a new database named `finance_tracker`
3. Run the schema (optional - Spring Boot will auto-create tables):
```sql
-- Tables will be created automatically by Spring Boot
-- See database_schema.sql for reference
```

### Configure Connection
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/finance_tracker
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD_HERE
```

---

## Run the Application

### Option 1: Using Maven Wrapper (Recommended - No Maven install needed)

**Windows:**
```bash
mvnw.cmd spring-boot:run
```

**Mac/Linux:**
```bash
./mvnw spring-boot:run
```

### Option 2: Using installed Maven
```bash
mvn spring-boot:run
```

---

## Verify It's Running

1. Open browser: [http://localhost:8080](http://localhost:8080)
2. API Documentation: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
3. Test endpoint:
```bash
curl http://localhost:8080/groups
```

---

## Common Issues

### Port 8080 already in use
Change port in `application.properties`:
```properties
server.port=8081
```

### Database connection failed
- Verify PostgreSQL is running
- Check username/password in `application.properties`
- Ensure database `finance_tracker` exists

### Java version mismatch
- This project requires Java 17
- Check: `java -version`

---

## Project Structure

```
finance_tracker_backend/
├── src/main/java/           # Java source code
├── src/main/resources/      # Configuration files
├── pom.xml                  # Maven dependencies
├── mvnw.cmd                 # Maven wrapper (Windows)
└── split.md                 # API documentation
```

---

## Next Steps

- Read [split.md](split.md) for API endpoints and examples
- Use Swagger UI for interactive API testing
- Check [QUICKSTART.md](QUICKSTART.md) for more features
