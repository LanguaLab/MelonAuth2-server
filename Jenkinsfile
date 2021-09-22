pipeline {
    agent any
    stages {
        stage('Build') {
            tools {
                jdk "jdk11"
            }
            steps {
                sh 'mvn package'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/MelonAuth2-Server-*.jar', fingerprint: true
            cleanWs()
        }
    }
}