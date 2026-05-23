# CampConnect Webhooks and Notifications

## GitHub Webhooks

Both Jenkins pipelines include:

```groovy
triggers {
    githubPush()
}
```

That makes Jenkins ready to react to GitHub push webhooks.

For local Jenkins, GitHub cannot reach `http://localhost:8080` directly. Use one of these exposure options:

- Development/demo: expose Jenkins with a tunnel such as ngrok, then use the HTTPS tunnel URL.
- Production/demo VM: use the public Jenkins URL on the VM or server.

GitHub webhook settings for both repositories:

```text
Payload URL: https://YOUR-JENKINS-URL/github-webhook/
Content type: application/json
Secret: optional but recommended
Events: Just the push event
Active: checked
```

Repositories to configure:

```text
Backend:  https://github.com/molk95/campConnect_backend
Frontend: https://github.com/samaalichayyma-ux/campconnect-frontend
```

After creating the webhook, push a small DevOps-only commit and confirm Jenkins starts the matching job automatically.

## Email Notifications

Both Jenkins pipelines now have an optional parameter:

```text
NOTIFICATION_EMAIL
```

When it is empty, no email is sent. When SMTP is configured in Jenkins and this parameter contains one or more recipients, Jenkins sends an email on:

- `SUCCESS`
- `UNSTABLE`
- `FAILURE`
- `ABORTED`

For failures and unstable builds, the build log is attached.

Jenkins SMTP setup:

```text
Manage Jenkins
-> System
-> Extended E-mail Notification
-> SMTP server, SMTP port, credentials, default sender
```

Recommended behavior for the project:

- Send emails on `FAILURE` and `UNSTABLE` during normal team work.
- Allow `SUCCESS` emails only during demo week or for final validation, because success emails can become noisy.
- Treat SonarQube Quality Gate failure as a pipeline failure, so the email tells the team when quality dropped.

Advanced improvement later:

- Add Warnings Next Generation plugin to publish compiler/linter warnings.
- Add a team email group instead of individual addresses.
- Add Slack, Teams, or Discord notifications for faster team alerts.
