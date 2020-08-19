pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh 'chmod +x build.sh'
                sh './build.sh'
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