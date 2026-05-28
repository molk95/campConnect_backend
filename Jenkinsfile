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
        string(name: 'DOCKERHUB_NAMESPACE', defaultValue: 'ihebboughanmi', description: 'Docker Hub namespace or username.')
        string(name: 'DOCKERHUB_CREDENTIALS_ID', defaultValue: 'dockerhub-credentials', description: 'Jenkins Docker Hub credentials ID.')
        booleanParam(name: 'DEPLOY_TO_K8S', defaultValue: false, description: 'Deploy the backend to Kubernetes after pushing the Docker image.')
        string(name: 'KUBECONFIG_CREDENTIALS_ID', defaultValue: 'kubeconfig-campconnect', description: 'Jenkins Secret file credential containing kubeconfig.')
        string(name: 'K8S_NAMESPACE', defaultValue: 'campconnect', description: 'Kubernetes namespace.')
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
                            '-Dsonar.coverage.exclusions=**/DTO/**,**/Entity/**,**/Enum/**,**/Repository/**'
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
                    def tag = env.GIT_COMMIT ? env.GIT_COMMIT.take(12) : "build-${env.BUILD_NUMBER}"
                    env.DOCKER_IMAGE_TAG = tag
                    env.DOCKER_IMAGE_REPO = "${params.DOCKERHUB_NAMESPACE}/${env.DOCKER_IMAGE_NAME}"

                    runCommand("docker build --pull -t ${env.DOCKER_IMAGE_REPO}:${env.DOCKER_IMAGE_TAG} -t ${env.DOCKER_IMAGE_REPO}:latest .")
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
                                        docker push "$DOCKER_IMAGE_REPO:latest"
                                    '''
                                } else {
                                    bat '''
                                        @echo off
                                        echo %DOCKERHUB_TOKEN% | docker login -u %DOCKERHUB_USERNAME% --password-stdin
                                        docker push %DOCKER_IMAGE_REPO%:%DOCKER_IMAGE_TAG%
                                        docker push %DOCKER_IMAGE_REPO%:latest
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
                                kubectl -n "$K8S_NAMESPACE" rollout status deployment/campconnect-backend --timeout=180s
                            '''
                        } else {
                            bat '''
                                @echo off
                                set KUBECONFIG=%KUBECONFIG_FILE%
                                kubectl apply -k devops/k8s
                                kubectl -n %K8S_NAMESPACE% set image deployment/campconnect-backend backend=%DOCKER_IMAGE_REPO%:%DOCKER_IMAGE_TAG%
                                kubectl -n %K8S_NAMESPACE% rollout status deployment/campconnect-backend --timeout=180s
                            '''
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts allowEmptyArchive: true, fingerprint: true, artifacts: 'target/*.jar'
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
