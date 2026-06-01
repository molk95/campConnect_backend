# CampConnect Configuration Management

This folder is the Configuration Management layer for CampConnect.

It uses Ansible to describe and repeat the infrastructure state instead of relying only on manual clicks.

## What It Manages

- Docker Engine and Docker Compose plugin on a Debian/Ubuntu DevOps host.
- Jenkins, SonarQube, and the SonarQube PostgreSQL database using `devops/docker-compose.jenkins-sonarqube.yml`.
- Kubernetes workload state for MySQL, backend, frontend, services, PVCs, and ingress.
- Runtime values through Ansible variables instead of hardcoding environment-specific settings in the app code.

## Files

- `inventory/campconnect.ini`: target hosts or local demo targets.
- `inventory/group_vars/campconnect.yml`: environment variables for the CM layer.
- `playbooks/provision-devops-host.yml`: installs Docker and starts Jenkins/SonarQube.
- `playbooks/deploy-k8s.yml`: applies Kubernetes manifests and updates backend/frontend images.
- `playbooks/full-cm.yml`: runs provisioning and Kubernetes deployment together.
- `templates/sonarqube.env.j2`: writes the local Compose `.env` used by SonarQube.

## First-Time Setup

Run Ansible from Linux, WSL, or a Linux VM:

```bash
cd /mnt/c/Users/ihebb/Desktop/PIDEV2026/campConnect_backend/devops/ansible
ansible-galaxy collection install -r requirements.yml
```

Edit `inventory/campconnect.ini` when using a real VM:

```ini
[devops_hosts]
campconnect-vm ansible_host=192.168.56.20 ansible_user=vagrant

[kubernetes_control]
campconnect-vm ansible_host=192.168.56.20 ansible_user=vagrant
```

Edit repo paths in `inventory/group_vars/campconnect.yml` or pass them as environment variables:

```bash
export CAMP_CONNECT_BACKEND_REPO=/mnt/c/Users/ihebb/Desktop/PIDEV2026/campConnect_backend
export CAMP_CONNECT_FRONTEND_REPO=/mnt/c/Users/ihebb/Desktop/PIDEV2026/campconnect-frontend
```

## Run The DevOps Host CM

```bash
ansible-playbook -i inventory/campconnect.ini playbooks/provision-devops-host.yml
```

After it finishes:

```text
Jenkins:   http://localhost:8080
SonarQube: http://localhost:9000
```

For a real VM, replace `localhost` with the VM IP.

## Run The Kubernetes CM

The target host must already have `kubectl` configured for the cluster.

```bash
ansible-playbook -i inventory/campconnect.ini playbooks/deploy-k8s.yml
```

Deploy a specific Docker Hub image tag:

```bash
ansible-playbook -i inventory/campconnect.ini playbooks/deploy-k8s.yml \
  -e backend_image_tag=e84508dbcb75 \
  -e frontend_image_tag=3ef2472d9eb2
```

## Security Notes

- Do not commit real passwords or tokens.
- Put real secrets in Ansible Vault, Jenkins credentials, or Kubernetes secrets.
- The example `sonar_jdbc_password` is intentionally a placeholder.

## Professor Demo Explanation

The CI/CD pipeline builds, tests, analyzes, and publishes images. This CM layer makes the environment reproducible:

```text
Git repo -> Ansible CM -> Docker/Jenkins/SonarQube/Kubernetes configured state
```

That means a new host can be configured from files, and Kubernetes can be reconciled back to the desired state without manually recreating every service.
