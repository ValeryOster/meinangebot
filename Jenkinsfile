pipeline {
    agent any
    tools {
            maven 'mvn3.6.0'
            jdk 'jdk8'
        }
        stages {
            stage ('Initialize') {
                steps {
                    sh '''
                        echo "PATH = ${PATH}"
                        echo "M2_HOME = ${M2_HOME}"
                    '''
                }
            }

    stages {
        stage('Build') {
            steps {
               echo 'This is a minimal pipeline.'
            }
        }
    }
}
