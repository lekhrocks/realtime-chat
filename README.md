# Real-Time Chat Application

A modern, production-ready real-time chat and file-sharing backend built with **Java 21**, **Spring Boot 3.x**, **GraphQL**, **WebSockets**, **REST**, and **AWS S3**.

---

## Features

- **User Management**
  - Registration, email verification, password reset, profile, avatar upload
  - User roles/permissions (USER, ADMIN)
  - User preferences (notification settings, file visibility, etc.)

- **Authentication & Security**
  - JWT authentication (REST, GraphQL, WebSocket)
  - CORS, HTTPS, rate limiting, audit logging
  - Password hashing (BCrypt), email verification

- **Real-Time Messaging**
  - WebSocket and GraphQL Subscriptions
  - Message types: text, image, file
  - Input validation, filtering, pagination, search

- **File Management**
  - Upload to S3, virus scanning (ClamAV), quotas, sharing, access control
  - File deletion, listing, signed URLs, per-user folders
  - Avatar upload and validation

- **Notifications**
  - Persistent, email, and WebSocket notifications
  - Pagination, search, mark as read, delete, bulk actions

- **Admin Dashboard**
  - User, file, quota, and system health monitoring
  - Audit log display (search, pagination)
  - Role management, user profile editing

- **Monitoring & Observability**
  - Actuator endpoints (health, liveness, readiness, Prometheus metrics)
  - Centralized logging (ELK/EFK/cloud)
  - Dockerized, Flyway migrations, production profiles

---

## System Design Diagram

```mermaid
flowchart TD
  User[Web/Frontend]
  Backend[Spring Boot App]
  REST[REST API (Swagger)]
  GraphQL[GraphQL API (Playground)]
  WS[WebSocket]
  Admin[Admin Dashboard]
  Actuator[Actuator/Prometheus]
  Audit[Audit Log Service]
  Notif[Notification Service]
  UserSvc[User Service]
  FileSvc[File Service]
  DB[(PostgreSQL)]
  Cache[(Redis)]
  S3[(S3)]
  ClamAV[(ClamAV)]
  SMTP[(SMTP)]
  Prom[(Prometheus/Grafana)]
  Logs[(ELK/EFK)]

  User-->|REST/GraphQL|REST
  User-->|GraphQL|GraphQL
  User-->|WebSocket|WS
  User-->|Admin|Admin
  REST-->|Service|UserSvc
  GraphQL-->|Service|UserSvc
  WS-->|Notifications|Notif
  Admin-->|Audit|Audit
  UserSvc-->|DB|DB
  UserSvc-->|Cache|Cache
  FileSvc-->|S3|S3
  FileSvc-->|Virus Scan|ClamAV
  Notif-->|Email|SMTP
  Actuator-->|Metrics|Prom
  Backend-->|Logs|Logs
  Audit-->|Logs|Logs
  FileSvc-->|Audit|Audit
  UserSvc-->|Audit|Audit
  Notif-->|Audit|Audit
  Admin-->|Health|Actuator
  Actuator-->|Health|Prom
```

---

## API Documentation

- **REST API (Swagger UI):** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **GraphQL Playground:** [http://localhost:8080/graphiql](http://localhost:8080/graphiql)

---

## Code Samples

### REST: Register User

```sh
curl -X POST "http://localhost:8080/api/user/register" -d "username=alice&email=alice@example.com&password=secret"
```

### REST: Login

```sh
curl -X POST "http://localhost:8080/api/user/login" -d "username=alice&password=secret"
```

### GraphQL: Send Message

```graphql
mutation {
  message(body: "Hello!", to: "bob", type: "text", token: "Bearer <JWT>") {
    items { from to body sentAt messageType }
  }
}
```

### GraphQL: Subscribe to Inbox

```graphql
subscription {
  inbox(to: "bob", token: "Bearer <JWT>") {
    items { from to body sentAt messageType }
  }
}
```

### File Upload (Avatar)

```sh
curl -X POST "http://localhost:8080/api/user/upload-avatar" -F "username=alice" -F "file=@avatar.png"
```

### Postman Collection

- Export from Swagger UI or see `postman_collection.json` (to be included).

---

## Setup & Environment

1. **Clone the repo:**
   ```sh
   git clone <repo-url>
   cd realtime-chat
   ```
2. **Configure environment variables:**
   - See `src/main/resources/application.properties` for required secrets (JWT, DB, AWS, email, etc.)
   - Use `.env` or your orchestrator's secret manager for production
3. **Run with Docker Compose:**
   ```sh
   docker-compose up --build
   ```
   - Services: app, PostgreSQL, Redis, ClamAV, (optionally NGINX for HTTPS)
4. **Database migrations:**
   - Flyway runs automatically on startup

---

## Deployment

- **Production:**
  - Set `SPRING_PROFILES_ACTIVE=prod`
  - Use a real SMTP server, S3 bucket, and secure secrets
  - Use a reverse proxy (NGINX, ALB) for HTTPS
  - Configure Prometheus/Grafana for monitoring
  - Forward logs to ELK/EFK/cloud logging

---

## Troubleshooting

- **Health checks:**
  - [http://localhost:8081/actuator/health](http://localhost:8081/actuator/health)
  - [http://localhost:8081/actuator/prometheus](http://localhost:8081/actuator/prometheus)
- **Common issues:**
  - Check Docker logs for errors
  - Ensure all secrets are set via environment variables
  - Check S3, SMTP, and DB connectivity

---

## Contributing

- PRs welcome! Please add tests and update documentation.

---

**For more details, see the code, Swagger UI, and GraphQL Playground.**
