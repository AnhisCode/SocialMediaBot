pipeline {
    agent any

    stages {
//     Make sure to set JWT_TOKEN in Jenkins env config
        stage('Set Env Variables') {
            steps {
               sh 'echo "DISCORDTOKEN=${DISCORDTOKEN}" > ./app/.env'
               sh 'echo "TWITCHTOKEN=${TWITCHTOKEN}" >> ./app/.env'
               sh 'echo "DBLOCATION=${DBLOCATION}" >> ./app/.env'
            }
        }
        stage('Build & Deploy') {
            steps {
                sh 'gradle build run'
            }
        }
    }
}