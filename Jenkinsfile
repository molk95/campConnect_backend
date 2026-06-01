def runMaven(String goals) {
    if (isUnix()) {
        sh "chmod +x mvnw && ./mvnw ${goals}"
    } else {
        bat "mvnw.cmd ${goals}"
    }
}

def runCommand(String command) {
    if (isUnix()) {
        sh command
    } else {
        bat command
    }
}

def runCommandStdout(String command) {
    if (isUnix()) {
        return sh(script: command, returnStdout: true).trim()
    }

    return bat(script: "@echo off\r\n${command}", returnStdout: true).trim()
}

def resolveProjectVersion() {
    def pomText = readFile('pom.xml')
    def matcher = pomText =~ /(?s)<artifactId>\s*campConnect\s*<\/artifactId>\s*<version>\s*([^<]+)\s*<\/version>/
    return matcher.find() ? matcher.group(1).trim() : '0.0.0-SNAPSHOT'
}

def safeDockerTag(String value) {
    def cleaned = (value ?: 'dev').trim()
        .replaceAll(/[^A-Za-z0-9_.-]/, '-')
        .replaceAll(/^[.-]+/, '')
        .replaceAll(/[.-]+$/, '')

    return cleaned ? cleaned.take(128) : 'dev'
}

def safeKubernetesLabel(String value) {
    def cleaned = safeDockerTag(value)
    return cleaned.take(63)
}

def notifyBuild(String resultLabel) {
    def recipients = params.NOTIFICATION_EMAIL?.trim()
    if (!recipients) {
        return
    }

    try {
        emailext(
            to: recipients,
            subject: "[CampConnect Backend] ${resultLabel}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            mimeType: 'text/html',
            attachLog: resultLabel != 'SUCCESS',
            body: """
                <p><b>CampConnect Backend</b> pipeline finished with status: <b>${resultLabel}</b>.</p>
                <p>
                    Job: ${env.JOB_NAME}<br/>
                    Build: #${env.BUILD_NUMBER}<br/>
                    Version: ${env.RELEASE_VERSION ?: 'n/a'}<br/>
                    Image: ${env.DOCKER_IMAGE_REPO && env.DOCKER_IMAGE_TAG ? "${env.DOCKER_IMAGE_REPO}:${env.DOCKER_IMAGE_TAG}" : 'n/a'}<br/>
                    Commit: ${env.GIT_COMMIT ?: 'n/a'}<br/>
                    URL: <a href="${env.BUILD_URL}">${env.BUILD_URL}</a>
                </p>
            """
        )
    } catch (err) {
        echo "Email notification failed: ${err}"
    }
}

pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    triggers {
        githubPush()
        pollSCM('H/2 * * * *')
    }

    parameters {
        string(name: 'SONARQUBE_SERVER', defaultValue: 'SonarQube', description: 'Jenkins SonarQube server name.')
        booleanParam(name: 'RUN_SONAR', defaultValue: true, description: 'Run SonarQube analysis.')
        booleanParam(name: 'ENFORCE_QUALITY_GATE', defaultValue: true, description: 'Fail the pipeline when the SonarQube quality gate fails.')
        booleanParam(name: 'BUILD_DOCKER_IMAGE', defaultValue: true, description: 'Build the backend Docker image.')
        booleanParam(name: 'PUSH_DOCKER_IMAGE', defaultValue: true, description: 'Push the backend Docker image to Docker Hub.')
        string(name: 'RELEASE_VERSION', defaultValue: 'auto', description: 'Use auto for pom version + build metadata, or provide a release version such as 1.2.0.')
        booleanParam(name: 'PUBLISH_LATEST', defaultValue: false, description: 'Also publish/move the latest tag. Keep false for traceable releases.')
        string(name: 'DOCKERHUB_NAMESPACE', defaultValue: 'ihebboughanmi', description: 'Docker Hub namespace or username.')
        string(name: 'DOCKERHUB_CREDENTIALS_ID', defaultValue: 'dockerhub-credentials', description: 'Jenkins Docker Hub credentials ID.')
        booleanParam(name: 'DEPLOY_TO_K8S', defaultValue: false, description: 'Deploy the backend to Kubernetes after pushing the Docker image.')
        string(name: 'KUBECONFIG_CREDENTIALS_ID', defaultValue: 'kubeconfig-campconnect', description: 'Jenkins Secret file credential containing kubeconfig.')
        string(name: 'K8S_NAMESPACE', defaultValue: 'campconnect', description: 'Kubernetes namespace.')
        string(name: 'ROLLOUT_TIMEOUT', defaultValue: '300s', description: 'Kubernetes rollout timeout for the backend deployment.')
        string(name: 'NOTIFICATION_EMAIL', defaultValue: 'ihebboughanmi17@gmail.com', description: 'Optional email recipients for pipeline notifications. Leave empty to disable.')
    }

    environment {
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
        SONAR_PROJECT_KEY = 'campconnect-backend'
        SONAR_PROJECT_NAME = 'CampConnect Backend'
        DOCKER_IMAGE_NAME = 'campconnect-backend'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Resolve Version') {
            steps {
                script {
                    def projectVersion = resolveProjectVersion()
                    def shortSha = env.GIT_COMMIT ? env.GIT_COMMIT.take(12) : runCommandStdout('git rev-parse --short=12 HEAD')
                    def fullSha = env.GIT_COMMIT ?: runCommandStdout('git rev-parse HEAD')
                    def exactGitTag = ''

                    try {
                        exactGitTag = runCommandStdout('git describe --tags --exact-match')
                    } catch (ignored) {
                        exactGitTag = ''
                    }

                    def requestedVersion = params.RELEASE_VERSION?.trim()
                    def baseVersion = requestedVersion && requestedVersion != 'auto' ? requestedVersion : (exactGitTag ?: projectVersion)
                    def isSnapshot = baseVersion.toUpperCase().contains('SNAPSHOT')
                    def releaseVersion = isSnapshot
                        ? "${baseVersion.replaceAll(/(?i)-SNAPSHOT$/, '')}-build.${env.BUILD_NUMBER}+${shortSha}"
                        : baseVersion

                    env.PROJECT_VERSION = projectVersion
                    env.RELEASE_VERSION = releaseVersion
                    env.GIT_SHORT_SHA = shortSha
                    env.GIT_FULL_SHA = fullSha
                    env.BUILD_DATE_UTC = new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone('UTC'))
                    env.DOCKER_IMAGE_REPO = "${params.DOCKERHUB_NAMESPACE}/${env.DOCKER_IMAGE_NAME}"
                    env.DOCKER_IMAGE_TAG = safeDockerTag(releaseVersion)
                    env.DOCKER_IMAGE_SHA_TAG = "sha-${shortSha}"
                    env.K8S_VERSION_LABEL = safeKubernetesLabel(env.DOCKER_IMAGE_TAG)

                    currentBuild.displayName = "#${env.BUILD_NUMBER} ${env.DOCKER_IMAGE_TAG}"
                    currentBuild.description = "${env.DOCKER_IMAGE_REPO}:${env.DOCKER_IMAGE_TAG}"

                    echo "Project version: ${env.PROJECT_VERSION}"
                    echo "Release version: ${env.RELEASE_VERSION}"
                    echo "Docker image: ${env.DOCKER_IMAGE_REPO}:${env.DOCKER_IMAGE_TAG}"
                    echo "Commit alias: ${env.DOCKER_IMAGE_REPO}:${env.DOCKER_IMAGE_SHA_TAG}"
                }
            }
        }

        stage('Build and Test') {
            steps {
                script {
                    runMaven('clean verify')
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'target/site/jacoco/jacoco.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            when {
                expression { params.RUN_SONAR }
            }
            steps {
                withSonarQubeEnv(params.SONARQUBE_SERVER) {
                    script {
                        def sonarArgs = [
                            'sonar:sonar',
                            "-Dsonar.projectKey=${env.SONAR_PROJECT_KEY}",
                            "\"-Dsonar.projectName=${env.SONAR_PROJECT_NAME}\"",
                            "-Dsonar.host.url=${env.SONAR_HOST_URL}",
                            "-Dsonar.token=${env.SONAR_AUTH_TOKEN}",
                            '-Dsonar.sources=src/main/java/com/esprit/campconnect/Event,src/main/java/com/esprit/campconnect/Reservation',
                            '-Dsonar.tests=src/test/java/com/esprit/campconnect/Event,src/test/java/com/esprit/campconnect/Reservation',
                            '-Dsonar.test.inclusions=**/*Test.java',
                            '-Dsonar.java.binaries=target/classes',
                            '-Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml',
                            '-Dsonar.coverage.exclusions=**/DTO/**,**/Entity/**,**/Enum/**,**/Repository/**',
                            '-Dsonar.issue.ignore.multicriteria=e1',
                            '-Dsonar.issue.ignore.multicriteria.e1.ruleKey=java:S120',
                            '-Dsonar.issue.ignore.multicriteria.e1.resourceKey=**/*.java'
                        ].join(' ')

                        runMaven(sonarArgs)
                    }
                }
            }
        }

        stage('Quality Gate') {
            when {
                expression { params.RUN_SONAR && params.ENFORCE_QUALITY_GATE }
            }
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Build') {
            when {
                expression { params.BUILD_DOCKER_IMAGE }
            }
            steps {
                script {
                    def sourceUrl = env.GIT_URL ?: 'https://github.com/molk95/campConnect_backend'
                    def dockerBuildCommand = [
                        'docker build --pull',
                        "--build-arg APP_VERSION=${env.RELEASE_VERSION}",
                        "--build-arg VCS_REF=${env.GIT_FULL_SHA}",
                        "--build-arg BUILD_DATE=${env.BUILD_DATE_UTC}",
                        "--build-arg IMAGE_SOURCE=${sourceUrl}",
                        "-t ${env.DOCKER_IMAGE_REPO}:${env.DOCKER_IMAGE_TAG}",
                        "-t ${env.DOCKER_IMAGE_REPO}:${env.DOCKER_IMAGE_SHA_TAG}",
                        '.'
                    ].join(' ')

                    runCommand(dockerBuildCommand)
                    writeFile file: 'target/release-metadata.properties', text: """project.version=${env.PROJECT_VERSION}
release.version=${env.RELEASE_VERSION}
docker.image=${env.DOCKER_IMAGE_REPO}:${env.DOCKER_IMAGE_TAG}
docker.shaAlias=${env.DOCKER_IMAGE_REPO}:${env.DOCKER_IMAGE_SHA_TAG}
kubernetes.versionLabel=${env.K8S_VERSION_LABEL}
git.sha=${env.GIT_FULL_SHA}
build.number=${env.BUILD_NUMBER}
build.date=${env.BUILD_DATE_UTC}
"""
                }
            }
        }

        stage('Docker Push') {
            when {
                expression { params.BUILD_DOCKER_IMAGE && params.PUSH_DOCKER_IMAGE }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: params.DOCKERHUB_CREDENTIALS_ID, usernameVariable: 'DOCKERHUB_USERNAME', passwordVariable: 'DOCKERHUB_TOKEN')]) {
                    script {
                        retry(3) {
                            timeout(time: 8, unit: 'MINUTES') {
                                if (isUnix()) {
                                    sh '''
                                        set +x
                                        echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin
                                        set -x
                                        trap 'docker logout || true' EXIT
                                        docker push "$DOCKER_IMAGE_REPO:$DOCKER_IMAGE_TAG"
                                        docker push "$DOCKER_IMAGE_REPO:$DOCKER_IMAGE_SHA_TAG"
                                        if [ "$PUBLISH_LATEST" = "true" ]; then
                                            docker tag "$DOCKER_IMAGE_REPO:$DOCKER_IMAGE_TAG" "$DOCKER_IMAGE_REPO:latest"
                                            docker push "$DOCKER_IMAGE_REPO:latest"
                                        fi
                                    '''
                                } else {
                                    bat '''
                                        @echo off
                                        echo %DOCKERHUB_TOKEN% | docker login -u %DOCKERHUB_USERNAME% --password-stdin
                                        docker push %DOCKER_IMAGE_REPO%:%DOCKER_IMAGE_TAG%
                                        docker push %DOCKER_IMAGE_REPO%:%DOCKER_IMAGE_SHA_TAG%
                                        if /I "%PUBLISH_LATEST%"=="true" (
                                            docker tag %DOCKER_IMAGE_REPO%:%DOCKER_IMAGE_TAG% %DOCKER_IMAGE_REPO%:latest
                                            docker push %DOCKER_IMAGE_REPO%:latest
                                        )
                                        docker logout
                                    '''
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Kubernetes Deploy') {
            when {
                expression { params.DEPLOY_TO_K8S && params.BUILD_DOCKER_IMAGE && params.PUSH_DOCKER_IMAGE }
            }
            steps {
                withCredentials([file(credentialsId: params.KUBECONFIG_CREDENTIALS_ID, variable: 'KUBECONFIG_FILE')]) {
                    script {
                        if (isUnix()) {
                            sh '''
                                set -e
                                export KUBECONFIG="$KUBECONFIG_FILE"
                                kubectl apply -k devops/k8s
                                kubectl -n "$K8S_NAMESPACE" set image deployment/campconnect-backend backend="$DOCKER_IMAGE_REPO:$DOCKER_IMAGE_TAG"
                                kubectl -n "$K8S_NAMESPACE" annotate deployment/campconnect-backend \
                                    campconnect.io/release-version="$RELEASE_VERSION" \
                                    campconnect.io/image="$DOCKER_IMAGE_REPO:$DOCKER_IMAGE_TAG" \
                                    campconnect.io/git-sha="$GIT_FULL_SHA" \
                                    campconnect.io/jenkins-build="$BUILD_NUMBER" \
                                    --overwrite
                                kubectl -n "$K8S_NAMESPACE" patch deployment/campconnect-backend --type merge -p "{\"spec\":{\"template\":{\"metadata\":{\"labels\":{\"app.kubernetes.io/version\":\"$K8S_VERSION_LABEL\"},\"annotations\":{\"campconnect.io/release-version\":\"$RELEASE_VERSION\",\"campconnect.io/image\":\"$DOCKER_IMAGE_REPO:$DOCKER_IMAGE_TAG\",\"campconnect.io/git-sha\":\"$GIT_FULL_SHA\",\"campconnect.io/jenkins-build\":\"$BUILD_NUMBER\"}}}}}"
                                kubectl -n "$K8S_NAMESPACE" rollout status deployment/campconnect-backend --timeout="$ROLLOUT_TIMEOUT"
                                kubectl -n "$K8S_NAMESPACE" rollout history deployment/campconnect-backend
                            '''
                        } else {
                            bat '''
                                @echo off
                                set KUBECONFIG=%KUBECONFIG_FILE%
                                kubectl apply -k devops/k8s
                                kubectl -n %K8S_NAMESPACE% set image deployment/campconnect-backend backend=%DOCKER_IMAGE_REPO%:%DOCKER_IMAGE_TAG%
                                kubectl -n %K8S_NAMESPACE% annotate deployment/campconnect-backend campconnect.io/release-version=%RELEASE_VERSION% campconnect.io/image=%DOCKER_IMAGE_REPO%:%DOCKER_IMAGE_TAG% campconnect.io/git-sha=%GIT_FULL_SHA% campconnect.io/jenkins-build=%BUILD_NUMBER% --overwrite
                                kubectl -n %K8S_NAMESPACE% patch deployment/campconnect-backend --type merge -p "{\"spec\":{\"template\":{\"metadata\":{\"labels\":{\"app.kubernetes.io/version\":\"%K8S_VERSION_LABEL%\"},\"annotations\":{\"campconnect.io/release-version\":\"%RELEASE_VERSION%\",\"campconnect.io/image\":\"%DOCKER_IMAGE_REPO%:%DOCKER_IMAGE_TAG%\",\"campconnect.io/git-sha\":\"%GIT_FULL_SHA%\",\"campconnect.io/jenkins-build\":\"%BUILD_NUMBER%\"}}}}}"
                                kubectl -n %K8S_NAMESPACE% rollout status deployment/campconnect-backend --timeout=%ROLLOUT_TIMEOUT%
                                kubectl -n %K8S_NAMESPACE% rollout history deployment/campconnect-backend
                            '''
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts allowEmptyArchive: true, fingerprint: true, artifacts: 'target/*.jar,target/release-metadata.properties'
        }
        success {
            script {
                notifyBuild('SUCCESS')
            }
        }
        unstable {
            script {
                notifyBuild('UNSTABLE')
            }
        }
        failure {
            script {
                notifyBuild('FAILURE')
            }
        }
        aborted {
            script {
                notifyBuild('ABORTED')
            }
        }
    }
}
