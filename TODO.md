---
title: "Containerization To-Do"
description: "Organized task list for containerizing the Pern-Merch application for ECS deployment"
ms.date: 2026-04-13
---

## Critical Blockers

These items will break container deployments if not addressed first.

- [ ] **Fix `ddl-auto=create-drop`** — Change to `validate` in production; every restart currently drops the full schema and all data
- [ ] **Externalize the DB URL** — Replace `localhost:5432` in `application.properties` with `${DB_HOST:localhost}` so the backend can reach a separate DB container or RDS instance
- [ ] **Externalize CORS origins** — Hardcoded `http://localhost:5173` in `SecurityConfig.java` will block all browser traffic in production; drive it with an env var
- [ ] **Externalize active profile** — Remove `spring.profiles.active=dev` from `application.properties`; control it via `SPRING_PROFILES_ACTIVE` environment variable to prevent the data seeder from running in production

## Phase 1: Local Containerization

Get the stack running end-to-end with Docker Compose on developer machines.

- [ ] Write multi-stage `Dockerfile` for the Spring Boot backend (Maven build → slim JRE runtime image)
- [ ] Write multi-stage `Dockerfile` for the React frontend (Node build → Nginx static server)
- [ ] Write `nginx.conf` for the frontend container to serve the Vite build output and reverse-proxy `/api` to the backend
- [ ] Write `docker-compose.yml` with `postgres`, `backend`, and `frontend` services wired together
- [ ] Add `backend/.dockerignore` (exclude `target/`, `.mvn/`, `*.iml`, etc.)
- [ ] Add `frontend/.dockerignore` (exclude `node_modules/`, `dist/`, `.env*`, etc.)

## Phase 2: Production Readiness

Harden the application for a real deployment.

- [ ] Add Spring Boot Actuator (`spring-boot-starter-actuator`) for a `/actuator/health` endpoint used by ECS health checks
- [ ] Add Flyway dependency and write an initial migration (`V1__init_schema.sql`) that matches the current JPA entity definitions
- [ ] Create `application-prod.properties` with safe production defaults (`show-sql=false`, `format_sql=false`, `ddl-auto=validate`)
- [ ] Document all required environment variables with expected format and example values

## Phase 3: ECS Deployment

Stand up the infrastructure and CI/CD pipeline.

- [ ] Create ECR repositories for backend and frontend images
- [ ] Provision an RDS PostgreSQL instance (or Aurora Serverless v2)
- [ ] Store secrets in AWS Secrets Manager (`JWT_SECRET`, `ADMIN_USERNAME`, `ADMIN_PASSWORD`, DB credentials)
- [ ] Define ECS task definitions for backend and frontend (Fargate recommended)
- [ ] Configure an Application Load Balancer with target groups and health check paths
- [ ] Set up VPC, subnets, and security groups with principle of least privilege
- [ ] Set up a GitHub Actions CI/CD pipeline: build images, push to ECR, deploy to ECS
