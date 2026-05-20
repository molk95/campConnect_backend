pipeline {
    agent any

    environment {
        SONAR_HOST_URL = 'http://campconnect-sonarqube:9000'
        SONAR_TOKEN = credentials('sonar-token')
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Backend') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                sh '''
                    mvn sonar:sonar \
                    -Dsonar.projectKey=campconnect \
                    -Dsonar.host.url=$SONAR_HOST_URL \
                    -Dsonar.token=$SONAR_TOKEN
                '''
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker compose build backend'
            }
        }

        stage('Deploy App') {
            steps {
                sh 'docker compose up -d'
            }
        }

        stage('Deploy Monitoring') {
            steps {
                sh 'docker compose -f docker-compose.monitoring.yml up -d'
            }
        }
    }
}