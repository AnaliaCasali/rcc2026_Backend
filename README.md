# Red Cultural Conecta — Backend API

REST API for **Red Cultural Conecta (RCC2026)**, a cultural platform that connects artists, groups, event organizers and audiences in local communities.

Built at ISP 63 (Argentina) as a project-based learning initiative with 3rd-year Software Development students, in partnership with EESO Nº 562 "Duncan L. Wagner" (Tacuarendí). Technical lead: [Analía Casali](https://www.linkedin.com/in/analiacasali).

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Validation, Mail) |
| Security | Spring Security + JWT (jjwt 0.13), role-based access |
| Database | MySQL 8.4 |
| API docs | springdoc-openapi 3 (Swagger UI) |
| Testing | JUnit 5, Spring Security Test, Testcontainers (MySQL), JaCoCo |
| Images | Thumbnailator + WebP |
| DevOps | Docker (multi-stage), Docker Compose, GitHub Actions, GHCR |

## Features

- **Authentication:** registration for users, groups and organizers; login with JWT; email account verification; password reset and change.
- **Profiles:** profile editing, profile images, social network links, awards.
- **Events:** create, edit and list events; upcoming events and calendar; group invitations with accept/decline flow; event permissions.
- **Social feed:** posts with images, likes, follows and followers.
- **Notifications:** unread count and mark as read.
- **Search and map:** profile search and locations map.

## Architecture

Layered architecture with clear separation of concerns:

```
src/main/java/edu/ies63/rcc2026_backend/
├── controller/   REST endpoints (/api/v1/...)
├── service/      Business logic (interfaces + impl)
├── repository/   Spring Data JPA repositories
├── model/        JPA entities
├── dto/          Request/response DTOs (entities never exposed)
├── security/     JWT filter, auth service and controller
├── exception/    Global exception handler
├── config/       OpenAPI, CORS/Web MVC, seed data
└── enums/        Domain enums (roles, event status, etc.)
```

## API overview

All endpoints are versioned under `/api/v1`.

| Resource | Base path |
|---|---|
| Auth | `/api/v1/auth` |
| Users | `/api/v1/auth/usuarios` |
| Events | `/api/v1/eventos` |
| Posts | `/api/v1/posts` |
| Likes | `/api/v1/posts/{postId}/likes` |
| Follows | `/api/v1/seguidos` |
| Notifications | `/api/v1/notificaciones` |
| Search | `/api/v1/busqueda` |
| Locations | `/api/v1/localidades` |

Interactive documentation is available at `/swagger-ui.html` when the app is running.

## Getting started

### Requirements

- Docker and Docker Compose, **or** JDK 25 + MySQL 8

### Run with Docker

```bash
cp .env.example .env        # then fill in your values
docker compose up --build
```

The API runs at `http://localhost:8080` and MySQL is exposed on port `3307`.

### Run locally

```bash
cp .env.example .env
./mvnw spring-boot:run
```

### Environment variables

See [`.env.example`](.env.example). Main variables:

| Variable | Description |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | MySQL connection |
| `JWT_SECRET` | Signing key, minimum 32 characters (`openssl rand -base64 32`) |
| `JWT_EXPIRATION` | Token lifetime in milliseconds |
| `MAIL_*` | SMTP settings for verification and password reset emails |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins (default `http://localhost:4200`) |

## Testing

56 test classes covering:

- **Unit tests:** services, controllers, DTOs, entities, JWT service and filter.
- **Integration tests:** auth, users, events, posts, likes, follows, notifications, search and locations, running against a real MySQL with Testcontainers.

```bash
./mvnw test              # Docker must be running for integration tests
./mvnw verify            # includes JaCoCo coverage report
```

Coverage report: `target/site/jacoco/index.html`.

## CI/CD

- **CI** (`ci-cd.yml`): on every push and pull request to `main`, builds the project with JDK 25, starts the full stack with Docker Compose and runs an HTTP smoke test.
- **CD** (`deploy.yml`): after a successful CI run on `main`, builds the Docker image and publishes it to GitHub Container Registry (`ghcr.io/analiacasali/rcc2026_backend`).

## Team

Developed by 3rd-year students of the Software Development program at ISP 63, Las Toscas, Santa Fe, Argentina, across the courses Software Project Management, Databases II and Programming II, using Scrum with user stories in Jira.

Technical lead: **Analía Casali** — [LinkedIn](https://www.linkedin.com/in/analiacasali)
