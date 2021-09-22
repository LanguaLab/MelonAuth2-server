pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                mvn package
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