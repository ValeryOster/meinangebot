pipeline {
    agent any
     tools {
        maven 'M3'
        jdk 'jdk8'
    }

    stages {
        stage('Checkout') {
            steps {
                git credentialsId: 'github-ssh-key',
                    url:'git@github.com:ValeryOster/meinangebot.git'
            }
        }
        stage('Build') {
            steps {
                // Run Maven on a Unix agent.
                sh "mvn install"
                sh "java -version"
            }
        }
        stage('Deploy'){
            steps{
                sh "mv backend/target/angebotapp.jar /var/www/http"
            }
        }
    }
}
