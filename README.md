# Pern-Merch

## Deployment Strategy (Lean and Professional)

### Planned Architecture

1. React frontend built and hosted on S3
2. CloudFront in front of S3 for CDN + HTTPS delivery
3. Spring Boot API deployed on ECS (Fargate Express mode)
4. Managed PostgreSQL on Neon

### Why This Is Beneficial

1. Low monthly cost
	Uses managed services and small task sizes, keeping costs low for early-stage traffic.

2. Professional deployment pattern
	Static frontend on a CDN, containerized backend, and managed database are standard production choices.

3. Easy to operate as a beginner
	Each component is managed and isolated, with minimal server maintenance.

4. Good performance for low-to-moderate traffic
	CloudFront caches assets globally, and the API runs in a dedicated container environment.

5. Easy upgrade path later
	Scale ECS tasks, add a load balancer, or migrate the database to a larger plan as traffic grows.

### Cost Expectation

- Typical range: about $12 to $30 per month (lean sizes + free DB tier)
- Higher if you add a load balancer, larger tasks, or higher traffic

### Tradeoffs (Known and Acceptable at Launch)

1. Small task sizes
	A 0.25 vCPU / 0.5 GB task can be tight for Java under load.

2. Managed service limits
	Free database tiers have connection and storage limits.

### Minimum Production Safeguards

1. HTTPS everywhere (CloudFront + API endpoint)
2. Secrets stored in a manager (not plain env vars)
3. DB SSL enabled in the JDBC URL
4. Basic monitoring and alerts

## Project Goal

This project uses a lean, production-grade AWS architecture with a managed database, keeping costs low while staying scalable.
