# Credentials Checklist

Do not commit these values to Git. Store them in Jenkins, SonarQube, or GitHub settings.

## Jenkins credentials

| Credential ID | Type | Used for | Required when |
| --- | --- | --- | --- |
| `github-campconnect-token` | Username with password | Jenkins checkout and webhook integration with GitHub | The GitHub repos are private, or Jenkins must manage webhooks |

For `github-campconnect-token`:

- Username: your GitHub username
- Password: a GitHub personal access token
- Required repository access:
  - `molk95/campConnect_backend`
  - `samaalichayyma-ux/campconnect-frontend`

If the repos are private and owned by colleagues, your GitHub account must be added as a collaborator or team member on both repositories.

## SonarQube token

Create one SonarQube token for Jenkins and attach it to the Jenkins SonarQube server configuration.

Recommended Jenkins configuration:

- Server name: `SonarQube`
- Server URL from Jenkins container: `http://sonarqube:9000`
- Token owner: a SonarQube user with permission to analyze both projects

SonarQube projects:

| Project key | Project name |
| --- | --- |
| `campconnect-backend` | `CampConnect Backend` |
| `campconnect-frontend` | `CampConnect Frontend` |

## GitHub webhooks

For automatic builds from GitHub, Jenkins needs a URL that GitHub can reach.

Local Jenkins at `http://localhost:8080` is not reachable from GitHub. Use one of these:

- A public Jenkins server URL
- A temporary tunnel such as ngrok or Cloudflare Tunnel for demos
- Jenkins polling while working locally

Webhook URL:

```text
https://YOUR_PUBLIC_JENKINS_URL/github-webhook/
```

Optional webhook secret:

```text
campconnect-github-webhook-secret
```

## Not needed yet

These belong to later phases:

- Docker Hub username/token
- Server SSH key
- Deployment environment secrets
- Database production credentials
