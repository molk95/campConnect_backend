# CampConnect Monitoring

This folder contains the Prometheus and Grafana monitoring layer.

## What It Monitors

The backend already exposes Spring Boot Actuator metrics through:

```text
http://localhost:8082/api/actuator/prometheus
```

Prometheus scrapes that endpoint from inside the Docker network at:

```text
backend:8082/api/actuator/prometheus
```

Grafana reads Prometheus and automatically loads the `CampConnect Backend Monitoring` dashboard.

## Start Locally

Start the main backend stack first:

```powershell
docker compose up -d
```

Then start monitoring:

```powershell
docker compose -f docker-compose.monitoring.yml up -d
```

The monitoring stack joins the backend Docker network. By default it uses:

```text
campconnect-network
```

If your older local backend stack uses another network, set:

```powershell
$env:CAMP_CONNECT_DOCKER_NETWORK="campconnect_backend_default"
docker compose -f docker-compose.monitoring.yml up -d
```

Open:

```text
Prometheus: http://localhost:9090
Grafana:    http://localhost:3000
```

Default Grafana login:

```text
Username: admin
Password: admin
```

For a cleaner demo password, create a local `.env` file with:

```text
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=change-me
```

Do not commit real passwords.

## Verify Prometheus

Open Prometheus and check:

```text
Status -> Targets
```

The `campconnect-backend` target should be `UP`.

Useful PromQL checks:

```promql
up{job="campconnect-backend"}
sum(rate(http_server_requests_seconds_count{job="campconnect-backend"}[1m]))
sum(jvm_memory_used_bytes{job="campconnect-backend",area="heap"})
```

## Alerts

Prometheus loads alert rules from:

```text
devops/monitoring/prometheus/alerts.yml
```

Current demo alerts:

- Backend down.
- Backend 5xx responses.
- JVM heap usage above 85 percent.

Without Alertmanager, these alerts are visible inside the Prometheus UI. Later, Alertmanager can send them to email, Slack, Teams, or Discord.

## How To Explain It

```text
Spring Boot Actuator -> Prometheus -> Grafana dashboard
```

This proves observability:

- Health and availability.
- API request rate.
- HTTP error rate.
- JVM memory usage.
- CPU usage.
- Tomcat thread activity.
