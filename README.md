# Pern-Merch

## Deployment Strategy (Cheap but Professional)

### Planned Architecture

1. EC2 (single Linux server) running Docker containers
2. Spring Boot API + PostgreSQL on the same EC2 instance
3. React frontend built and hosted on S3
4. CloudFront in front of S3 for CDN + HTTPS delivery

### Why This Is Beneficial

1. Low monthly cost
	This avoids paying for multiple managed services early. For launch-stage traffic, this is one of the cheapest AWS setups that still looks and behaves like a real production deployment.

2. Professional deployment pattern
	You still get production fundamentals: containerized backend, isolated frontend hosting, CDN caching, HTTPS, and environment-based configuration.

3. Simple to operate as a beginner
	One server is much easier to manage than a multi-service architecture when you are learning deployment and operations.

4. Good performance for low-to-moderate traffic
	CloudFront serves static assets quickly and reduces load on origin infrastructure. The backend and DB on one box is usually fine in early launch.

5. Easy upgrade path later
	When traffic grows, this architecture can evolve gradually:
	- move PostgreSQL to RDS
	- add load balancing
	- split backend into multiple instances

### Cost Expectation

- Typical range: about $13 to $30 per month
- Best case: can be near free-tier or close to $10/month for light usage

### Tradeoffs (Known and Acceptable at Launch)

1. Single server risk
	If the EC2 instance fails, API and DB are both affected.

2. More manual operations
	Backups, monitoring, and updates are your responsibility.

3. Limited immediate scaling
	This is not designed for high traffic from day one.

### Minimum Production Safeguards

1. Automated PostgreSQL backups to S3
2. Tight security groups (no public DB access)
3. HTTPS everywhere
4. Basic CloudWatch monitoring and alerts
5. Recovery checklist for redeploy/restore

## Project Goal

This project intentionally starts with a lean, budget-conscious AWS architecture that is suitable for early production while keeping future scaling options open.
