# CampConnect Configuration Management Overview

Configuration Management means the desired environment is stored as versioned files and can be applied repeatedly.

For CampConnect, CM is split into three levels.

## 1. Infrastructure And DevOps Tools

Managed by:

- `devops/ansible/playbooks/provision-devops-host.yml`
- `devops/docker-compose.jenkins-sonarqube.yml`

This layer installs Docker on a Linux DevOps host and starts:

- Jenkins
- SonarQube
- SonarQube PostgreSQL database

The goal is to avoid a manual-only Jenkins/SonarQube setup.

## 2. Application Runtime Configuration

Managed by:

- `devops/k8s/mysql.yaml`
- `devops/k8s/backend.yaml`
- `../campconnect-frontend/devops/k8s/frontend.yaml`

This layer defines:

- Namespace: `campconnect`
- MySQL `ConfigMap`, `Secret`, `PersistentVolumeClaim`, `Service`, and `Deployment`
- Backend `ConfigMap`, `Secret`, `PersistentVolumeClaim`, `Service`, and `Deployment`
- Frontend `Service` and `Deployment`
- Optional NodePort, LoadBalancer, and Ingress exposure

Pods communicate through Kubernetes Services:

```text
frontend pod -> campconnect-backend service -> backend pod
backend pod  -> mysql service               -> mysql pod
```

The backend does not need the MySQL pod IP. It uses the stable service DNS name:

```text
mysql.campconnect.svc.cluster.local
```

Inside the same namespace, `mysql` is enough.

## 3. Continuous Delivery State

Managed by:

- Backend `Jenkinsfile`
- Frontend `Jenkinsfile`
- `devops/ansible/playbooks/deploy-k8s.yml`

The Jenkins pipelines build exact image tags from Git commits and push them to Docker Hub.

When Kubernetes deployment is enabled, Jenkins updates the deployment image to the exact successful commit tag.

The Ansible Kubernetes playbook can do the same update manually or from a release process:

```bash
ansible-playbook -i inventory/campconnect.ini playbooks/deploy-k8s.yml \
  -e backend_image_tag=e84508dbcb75 \
  -e frontend_image_tag=3ef2472d9eb2
```

## What This Proves

- The environment is reproducible.
- Runtime configuration is separated from application code.
- Secrets are separated from normal config.
- Kubernetes Services provide stable pod-to-pod communication.
- Docker Hub images are traceable to Git commits.
- Jenkins handles CI/CD, while Ansible handles Configuration Management.
