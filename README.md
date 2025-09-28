# RAG Chat Storage Microservice

A production-ready Spring WebFlux microservice for storing RAG (Retrieval-Augmented Generation) chat sessions and messages. Built with Java 17, Spring Boot 3, and reactive programming principles.

## Features

- **Reactive Architecture**: Built with Spring WebFlux for high concurrency
- **Session Management**: Create, update, rename, favorite, and delete chat sessions
- **Message Storage**: Store user and assistant messages with optional RAG context
- **API Security**: API key authentication with configurable keys
- **Rate Limiting**: Configurable rate limiting to prevent API abuse
- **Pagination Support**: Efficient pagination for messages and sessions
- **Health Checks**: Comprehensive health monitoring endpoints
- **CORS Support**: Configurable CORS for web applications
- **OpenAPI Documentation**: Complete API documentation with Swagger UI
- **Global Error Handling**: Centralized error handling with detailed responses
- **Docker Support**: Complete containerization with Docker Compose

##  Tech Stack

- **Java 17**: Latest LTS version with modern language features
- **Spring Boot 3.2**: Latest Spring Boot with native support
- **Spring WebFlux**: Reactive web framework for high performance
- **Spring Data R2DBC**: Reactive database connectivity
- **H2 Database**: In-memory database for development
- **Lombok**: Reduce boilerplate code
- **Maven**: Dependency management and build tool
- **Docker**: Containerization platform
- **OpenAPI 3**: API documentation and testing

##  Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose (for containerized setup)

##  Project Structure

```
rag-chat-storage/
├── src/main/java/com/ragchat/storage/
│   ├── RagChatStorageApplication.java
│   ├── config/                 # Configuration classes
│   ├── controller/             # REST controllers
│   ├── dto/                    # Data transfer objects
│   ├── entity/                 # JPA entities
│   ├── exception/              # Exception handling
│   ├── filter/                 # Security filters
│   ├── repository/             # Data repositories
│   └── service/                # Business logic
├── src/main/resources/
│   ├── application.yml         # Application configuration
│   └── schema.sql              # Database schema
├── src/test/                   # Unit tests
├── Dockerfile                  # Container definition
├── docker-compose.yml          # Multi-service setup
├── pom.xml                     # Maven dependencies
└── README.md
```

##  Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/your-repo/rag-chat-storage.git
cd rag-chat-storage
```

### 2. Environment Configuration

```bash
cp .env.example .env
# Edit .env file with your configuration
```

### 3. Run with Docker Compose (Recommended)

```bash
docker-compose up --build
```

### 4. Run Locally with Maven

```bash
# Install dependencies
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

##  Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | 8080 | Application server port |
| `API_KEY` | default-api-key-change-in-production | API authentication key |
| `RATE_LIMIT_RPM` | 60 | Requests per minute per client |
| `RATE_LIMIT_CAPACITY` | 100 | Rate limit bucket capacity |
| `CORS_ALLOWED_ORIGINS` | * | Allowed CORS origins |
| `LOG_LEVEL` | INFO | Application logging level |
| `DB_LOG_LEVEL` | WARN | Database logging level |

### Application Profiles

- **default**: Uses H2 in-memory database
- **production**: Configure external database connection

##  API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Authentication
All API endpoints (except health checks) require an API key in the header:
```
X-API-Key: your-api-key
```

### Available Endpoints

#### Chat Sessions

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/sessions` | Create a new chat session |
| GET | `/sessions/{id}` | Get session by ID |
| GET | `/sessions?userId={userId}&page={page}&size={size}` | Get user sessions (paginated) |
| PUT | `/sessions/{id}` | Update session (rename/favorite) |
| DELETE | `/sessions/{id}` | Delete session and all messages |

#### Chat Messages

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/sessions/{sessionId}/messages` | Add message to session |
| GET | `/sessions/{sessionId}/messages?page={page}&size={size}` | Get session messages (paginated) |

#### Health & Monitoring

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/health` | Application health status |
| GET | `/api/v1/health/ping` | Simple ping endpoint |
| GET | `/actuator/health` | Spring Boot health indicators |

### Example API Calls

#### Create Session
```bash
curl -X POST http://localhost:8080/api/v1/sessions \
  -H "X-API-Key: your-api-key" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "title": "My RAG Chat"
  }'
```

#### Add Message
```bash
curl -X POST http://localhost:8080/api/v1/sessions/{sessionId}/messages \
  -H "X-API-Key: your-api-key" \
  -H "Content-Type: application/json" \
  -d '{
    "sender": "USER",
    "content": "What is artificial intelligence?",
    "context": "Retrieved context from knowledge base..."
  }'
```

#### Update Session
```bash
curl -X PUT http://localhost:8080/api/v1/sessions/{sessionId} \
  -H "X-API-Key: your-api-key" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "AI Discussion",
    "isFavorite": true
  }'
```

##  Testing

### Run Unit Tests
```bash
./mvnw test
```

### Run Integration Tests
```bash
./mvnw verify
```

### Test Coverage
```bash
./mvnw jacoco:report
```

##  Monitoring & Observability

### Health Checks
- **Application Health**: `/api/v1/health`
- **Database Health**: `/actuator/health`
- **Metrics**: `/actuator/metrics`

### Swagger UI
Access interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### H2 Console (Development)
Access H2 database console at:
```
http://localhost:8080/h2-console
```
- **JDBC URL**: `jdbc:h2:mem:ragchat`
- **Username**: `sa`
- **Password**: (empty)

## 🐳 Docker Services

The `docker-compose.yml` includes:

1. **rag-chat-storage**: Main application service
2. **h2-console**: H2 database console (port 8082)
3. **adminer**: Database management tool (port 8081)

### Docker Commands
```bash
# Build and start services
docker-compose up --build

# Start in background
docker-compose up -d

# View logs
docker-compose logs -f rag-chat-storage

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

##  Security Features

- **API Key Authentication**: Secure API access
- **Rate Limiting**: Prevent API abuse (configurable per client)
- **CORS Protection**: Configurable cross-origin policies
- **Input Validation**: Comprehensive request validation
- **Error Sanitization**: Secure error responses
- **Non-root Container**: Docker security best practices

##  Production Deployment

### Database Migration
For production, replace H2 with a persistent database:

1. **PostgreSQL Configuration**:
```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/ragchat
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

2. **MySQL Configuration**:
```yaml
spring:
  r2dbc:
    url: r2dbc:mysql://localhost:3306/ragchat
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### Environment Variables for Production
```bash
# Security
API_KEY=your-very-secure-api-key-here

# Database
DB_HOST=your-database-host
DB_PORT=5432
DB_NAME=ragchat
DB_USERNAME=ragchat_user
DB_PASSWORD=secure-password

# Performance
RATE_LIMIT_RPM=1000
RATE_LIMIT_CAPACITY=2000

# Logging
LOG_LEVEL=WARN
```

### Health Check Integration
Configure your load balancer/orchestrator to use:
```
http://your-service/api/v1/health/ping
```

##  Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

##  License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

##  Support

- **Documentation**: [API Docs](http://localhost:8080/swagger-ui.html)
- **Issues**: [GitHub Issues](https://github.com/your-repo/rag-chat-storage/issues)
- **Discussions**: [GitHub Discussions](https://github.com/your-repo/rag-chat-storage/discussions)

##  Roadmap

- [ ] Database migration support
- [ ] Message search functionality
- [ ] WebSocket support for real-time updates
- [ ] Message export/import features
- [ ] Advanced analytics and metrics
- [ ] Multi-tenancy support
- [ ] Message encryption at rest

---

**Built with ❤️ using Spring WebFlux and reactive programming principles.**
