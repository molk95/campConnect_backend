# Jenkins and SonarQube

This setup runs Jenkins, SonarQube Community, and PostgreSQL for local CI quality checks.

The Jenkins image is built locally with Java 17, Node.js 22, npm, Chromium, and the baseline Jenkins plugins needed by the backend and frontend pipelines.

Before wiring GitHub and SonarQube credentials, read `devops/credentials.md`.

## Start the tools

```bash
docker compose -f devops/docker-compose.jenkins-sonarqube.yml up -d
```

URLs:

- Jenkins: `http://localhost:8080`
- SonarQube: `http://localhost:9000`

Get the initial Jenkins password:

```bash
docker exec campconnect-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

SonarQube starts with `admin` / `admin` and asks you to change the password.

## Jenkins plugins

The local Jenkins image installs these plugins:

- Pipeline
- Git
- SonarQube Scanner for Jenkins
- JUnit
- Pipeline Stage View

The backend uses `mvnw`, so Jenkins does not need a global Maven installation.

Configure a SonarScanner tool in `Manage Jenkins > Tools`:

- Name: `SonarScanner`
- Install automatically: enabled

## SonarQube projects

Create two SonarQube projects:

| Project key | Project name |
| --- | --- |
| `campconnect-backend` | `CampConnect Backend` |
| `campconnect-frontend` | `CampConnect Frontend` |

Generate a SonarQube token for Jenkins.

## Jenkins SonarQube server

In Jenkins, go to `Manage Jenkins > System > SonarQube servers`:

- Name: `SonarQube`
- Server URL: `http://sonarqube:9000`
- Authentication token: the token generated in SonarQube

Add a SonarQube webhook:

```text
http://jenkins:8080/sonarqube-webhook/
```

The webhook is required for the `Quality Gate` stage in the `Jenkinsfile`.

## Pipeline jobs

Create one Jenkins Pipeline job for each repository.

Backend job:

- Job name: `campconnect-backend-ci`
- Repository URL: `https://github.com/molk95/campConnect_backend.git`
- Branch: `main`
- Script Path: `Jenkinsfile`

Frontend job:

- Job name: `campconnect-frontend-ci`
- Repository URL: `https://github.com/samaalichayyma-ux/campconnect-frontend.git`
- Branch: `main`
- Script Path: `Jenkinsfile`

The pipeline performs:

- Checkout from GitHub
- Backend Maven `clean verify`
- Frontend npm install and production build
- JUnit or artifact collection where available
- JaCoCo backend coverage generation
- SonarQube analysis
- SonarQube quality gate enforcement
- Artifact archiving

## Linux host note

On Linux hosts, SonarQube may need this kernel setting before startup:

```bash
sudo sysctl -w vm.max_map_count=262144
```
