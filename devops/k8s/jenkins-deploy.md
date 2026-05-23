# Jenkins Kubernetes Deployment

The Jenkins pipelines already include an optional `Kubernetes Deploy` stage. It is disabled by default with `DEPLOY_TO_K8S=false`.

Keep it disabled until a Kubernetes cluster and kubeconfig credential are ready.

## Local Kubernetes With Docker Desktop

Docker Desktop Kubernetes is currently expected for the local lab. Enable it manually:

1. Open Docker Desktop.
2. Go to `Settings`.
3. Go to `Kubernetes`.
4. Enable Kubernetes.
5. Click `Apply & Restart`.

Verify from PowerShell:

```powershell
docker desktop kubernetes status
kubectl config current-context
kubectl get nodes
```

The context should be a Docker Desktop Kubernetes context, commonly `docker-desktop`.

## Jenkins Kubeconfig Credential

After Kubernetes is enabled, add the kubeconfig to Jenkins:

1. Jenkins -> `Manage Jenkins`
2. `Credentials`
3. `System`
4. `Global credentials`
5. `Add Credentials`
6. Kind: `Secret file`
7. File: your kubeconfig file, usually `%USERPROFILE%\.kube\config`
8. ID: `kubeconfig-campconnect`
9. Description: `CampConnect Kubernetes kubeconfig`
10. Save

If Jenkins cannot reach the Kubernetes API from inside its container, create a Jenkins-specific copy of kubeconfig and change the cluster server from a localhost address to:

```text
https://host.docker.internal:6443
```

Then upload that adjusted copy as the `kubeconfig-campconnect` secret file.

## Manual Deploy Test

Before enabling Jenkins deployment, test manifests locally:

```powershell
kubectl apply -k devops/k8s
kubectl -n campconnect get pods,svc,pvc
kubectl -n campconnect rollout status deployment/mysql --timeout=180s
kubectl -n campconnect rollout status deployment/campconnect-backend --timeout=180s
```

Frontend manifests are in the frontend repository:

```powershell
cd ..\campconnect-frontend
kubectl apply -f devops/k8s/frontend.yaml
kubectl -n campconnect rollout status deployment/campconnect-frontend --timeout=180s
```

## Jenkins Deploy

When the manual deploy works, run the jobs with:

- `DEPLOY_TO_K8S=true`
- `KUBECONFIG_CREDENTIALS_ID=kubeconfig-campconnect`
- `K8S_NAMESPACE=campconnect`

The pipeline deploys the exact Docker image tag produced by the build, not just `latest`.
