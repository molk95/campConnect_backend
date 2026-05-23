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

pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    parameters {
        string(name: 'SONARQUBE_SERVER', defaultValue: 'SonarQube', description: 'Jenkins SonarQube server name.')
        booleanParam(name: 'RUN_SONAR', defaultValue: true, description: 'Run SonarQube analysis.')
        booleanParam(name: 'ENFORCE_QUALITY_GATE', defaultValue: true, description: 'Fail the pipeline when the SonarQube quality gate fails.')
        booleanParam(name: 'BUILD_DOCKER_IMAGE', defaultValue: true, description: 'Build the backend Docker image.')
        booleanParam(name: 'PUSH_DOCKER_IMAGE', defaultValue: true, description: 'Push the backend Docker image to Docker Hub.')
        string(name: 'DOCKERHUB_NAMESPACE', defaultValue: 'ihebboughanmi', description: 'Docker Hub namespace or username.')
        string(name: 'DOCKERHUB_CREDENTIALS_ID', defaultValue: 'dockerhub-credentials', description: 'Jenkins Docker Hub credentials ID.')
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
                            '-Dsonar.java.binaries=target/classes',
                            '-Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml'
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
                        if (isUnix()) {
                            sh '''
                                set +x
                                echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin
                                set -x
                                docker push "$DOCKER_IMAGE_REPO:$DOCKER_IMAGE_TAG"
                                docker push "$DOCKER_IMAGE_REPO:latest"
                                docker logout
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

    post {
        always {
            archiveArtifacts allowEmptyArchive: true, fingerprint: true, artifacts: 'target/*.jar'
        }
    }
}
