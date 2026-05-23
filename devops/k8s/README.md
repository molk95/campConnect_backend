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
