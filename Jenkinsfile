def runMaven(String goals) {
    if (isUnix()) {
        sh "chmod +x mvnw && ./mvnw ${goals}"
    } else {
        bat "mvnw.cmd ${goals}"
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
    }

    environment {
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
        SONAR_PROJECT_KEY = 'campconnect-backend'
        SONAR_PROJECT_NAME = 'CampConnect Backend'
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
    }

    post {
        always {
            archiveArtifacts allowEmptyArchive: true, fingerprint: true, artifacts: 'target/*.jar'
        }
    }
}
