# CampConnect Kubernetes Manifests

This folder contains the Kubernetes deployment layer for CampConnect.

## MySQL

`mysql.yaml` creates:

- `campconnect` namespace
- `mysql-secret` for the root password
- `mysql-config` for the database name
- `mysql-data` persistent volume claim
- `mysql` internal ClusterIP service
- `mysql` deployment using `mysql:8.3`

The service is intentionally named `mysql` because the backend Docker profile uses:

```properties
spring.datasource.url=jdbc:mysql://mysql:3306/campconnect?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false
```

No SQL seed/init files are mounted here yet. The current backend can create/update its own schema with Hibernate. When the team finishes integrating shared SQL or migrations, add them through a migration tool such as Flyway/Liquibase or a controlled init job.

Before production, change the `MYSQL_ROOT_PASSWORD` value in `mysql-secret`.

Apply locally:

```powershell
kubectl apply -f devops/k8s/mysql.yaml
kubectl -n campconnect get pods,svc,pvc
```

## Backend

`backend.yaml` creates:

- `backend-config` for non-secret runtime configuration
- `backend-secret` for application secrets
- `backend-uploads` persistent volume claim
- `campconnect-backend` internal service
- `campconnect-backend` deployment using `ihebboughanmi/campconnect-backend:latest`

`exposure.yaml` adds local/demo access services:

- `campconnect-backend-nodeport` exposes the backend on node port `30082`
- `campconnect-backend-local` exposes the backend on `localhost:8082` when the local Kubernetes provider supports `LoadBalancer` services, such as Docker Desktop

`ingress.yaml` defines the target HTTP routing model:

- `http://campconnect.local/` -> frontend service
- `http://campconnect.local/api` -> backend service

The Ingress requires an Ingress controller, such as ingress-nginx. Without a controller, the Ingress object can exist but it will not receive traffic.

The backend reads the MySQL password from `mysql-secret`, so the backend and MySQL manifests must agree on the same secret.

Apply MySQL + backend together:

```powershell
kubectl apply -k devops/k8s
kubectl -n campconnect get pods,svc,pvc
```

Local access after applying backend and frontend manifests:

```powershell
kubectl -n campconnect get svc
```

Expected demo URLs:

```text
Frontend NodePort: http://localhost:30080
Backend NodePort:  http://localhost:30082/api/actuator/health
Backend local LB:  http://localhost:8082/api/actuator/health, when LoadBalancer is supported by the local cluster
Ingress target:    http://campconnect.local
```

On some Docker Desktop/WSL setups, NodePort and local LoadBalancer services may not bind directly to Windows `localhost`. In that case, keep the same demo ports by running:

```powershell
kubectl -n campconnect port-forward svc/campconnect-frontend 30080:80
kubectl -n campconnect port-forward svc/campconnect-backend 30082:8082
```

Ingress controller setup for local Kubernetes:

```powershell
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.15.1/deploy/static/provider/baremetal/deploy.yaml
kubectl -n ingress-nginx rollout status deployment/ingress-nginx-controller --timeout=240s
```

Local Ingress test through port-forward:

```powershell
kubectl -n ingress-nginx port-forward svc/ingress-nginx-controller 8088:80
```

Then add this line to your Windows hosts file if you want to test in the browser:

```text
127.0.0.1 campconnect.local
```

Open:

```text
http://campconnect.local:8088
http://campconnect.local:8088/api/actuator/health
```

The Ingress also accepts `localhost`, so these URLs work while the port-forward is running:

```text
http://localhost:8088
http://localhost:8088/api/actuator/health
```

The current Angular code still contains several `http://localhost:8082` calls. For that reason, keep the backend reachable on `localhost:8082` during the current demo phase. Later, the cleaner production fix is to move the frontend to a single API base URL and route `/api` through Ingress.

The Jenkins pipeline updates the backend deployment image to the exact commit tag when Kubernetes deployment is enabled.
