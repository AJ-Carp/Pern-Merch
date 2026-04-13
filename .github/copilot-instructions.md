---
title: "Copilot Instructions"
description: "Project conventions and guidance for GitHub Copilot when working in the Pern-Merch workspace"
ms.date: 2026-04-13
---

## Project Overview

Pern-Merch is a full-stack band merchandise e-commerce application with a Spring Boot backend, a React
frontend, and a PostgreSQL database. The name is a nod to the PERN stack but the backend is intentionally
Java rather than Node.js.

## Tech Stack

| Layer    | Technology                                      |
|----------|-------------------------------------------------|
| Frontend | React 19, Vite, React Router v7                 |
| Backend  | Spring Boot 4, Java 21, Maven, Spring Security  |
| Database | PostgreSQL                                      |
| Auth     | JWT (jjwt 0.12.x), BCrypt password hashing      |

## Project Structure

```
backend/   Spring Boot Maven project
frontend/  Vite + React project
```

## Backend Conventions

- **Package root**: `com.ajcarpinello.Pern_Merch_Website`
- **Layers**: `controller` → `service` → `repository` → `entity`
- **DTOs** live in `dto/` and are the only types exposed by controllers; never return raw entity objects from an endpoint
- Use **Lombok** (`@Data`, `@Builder`, `@RequiredArgsConstructor`, `@NoArgsConstructor`, `@AllArgsConstructor`) on all entities and DTOs
- Use **`@Valid`** on controller method parameters that accept request bodies
- Annotate service methods that mutate data with **`@Transactional`**; use pessimistic locking (`findByIdForUpdate`) in `ProductRepository` when checking inventory during checkout
- Secrets and configurable values are always injected via **`@Value`** from environment variables; never hardcode credentials or URLs in source code
- The `dev` Spring profile activates `DataSeeder` (sample products); `AdminSeeder` runs on all profiles
- Error handling is centralized in `GlobalExceptionHandler`

## Frontend Conventions

- All HTTP calls go through **`src/api/api.js`**; do not use `fetch` or `axios` directly in components or pages
- The Vite dev server proxies `/api` to `http://localhost:8080`; all API paths start with `/api`
- Auth state (token, username, role) lives in **`AuthContext`** and is persisted in `localStorage`
- Cart state lives in **`CartContext`**
- Use **`ProtectedRoute`** for any page that requires authentication
- Pages live in `src/pages/`; shared UI components live in `src/components/`

## Environment Variables

### Backend (required at runtime)

| Variable             | Description                                      |
|----------------------|--------------------------------------------------|
| `JWT_SECRET`         | Base64-encoded HMAC-SHA key for signing JWTs     |
| `ADMIN_USERNAME`     | Username for the seeded admin account            |
| `ADMIN_PASSWORD`     | Password for the seeded admin account            |
| `DB_HOST`            | PostgreSQL hostname (default: `localhost`)       |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile (`dev` or `prod`)      |

## Known Issues to Address

See [TODO.md](../TODO.md) for the full prioritized list. Key items:

- `spring.jpa.hibernate.ddl-auto` is set to `create-drop`; this must be `validate` in production with Flyway managing schema
- CORS allowed origins are hardcoded in `SecurityConfig`; externalize before deploying
- No `Dockerfile` or `docker-compose.yml` exists yet
- Spring Boot Actuator is not included; needed for ECS health checks
