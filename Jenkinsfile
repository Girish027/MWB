node('jenkins-slave06.pool.sv2.247-inc.net') {

}

pipeline {

  agent { label 'docker'

  }

  environment {
    NEXUS_URL = "http://nexus.cicd.sv2.247-inc.net:5000"
    NEXUS_USER = "nexus-admin"
    PROJECT_NAME = "advancedprototypes-ui"
    DK_TAG = "${env.PROJECT_NAME}:${env.BUILD_NUMBER}.${currentBuild.timeInMillis}"
    DK_ROOT = "/usr/src/app"
    DK_BUILD_FILE = "${env.DK_ROOT}/docker.build.sh"
    DK_OUTPUT = "dk_output"
    MS_TEAMS_WEBHOOK_URL="https://outlook.office.com/webhook/1f6c331a-2a87-49b6-8fe6-9e5c88c4c2ce@42fbd5e8-b41c-40ab-9505-9ce8dd91c3e2/JenkinsCI/32c38caf1e1e4e15b89b2834e4cc7665/a966ba0a-e16d-4b4d-a7fb-fa637226e050"
  }

  stages {

    stage('start') {
      steps {
        slackSend channel: env.SLACK_CHANNEL,
                  color: '#FFFF00',
                  message: "STARTED: ${env.PROJECT_NAME}/${env.BRANCH_NAME} - (<${env.BUILD_URL}|Open>)"
      }
    }


    stage('info') {
      steps {
        sh "echo \"Environment Variables\"; set"
      }
    }

    stage('create tag') {
      steps {
        sh "docker build --tag ${env.DK_TAG} --build-arg GIT_BRANCH=${env.BRANCH_NAME} --force-rm ."
      }
    }
    stage('update image') {
      steps {
        sh "sed -E -i'' \"s/(image: )${env.PROJECT_NAME}:.*/\\1${env.DK_TAG}/\" docker-compose.yml"
      }
    }


   stage('build') {
      steps {
        sh "docker-compose run ui"
      }
    }
  }



  post {

    success {
      office365ConnectorSend message: "View <a href=\"${env.BUILD_URL}console\">console output</a>.",
                             status: "Success",
                             webhookUrl: env.MS_TEAMS_WEBHOOK_URL
    }

    failure {
      office365ConnectorSend message: "View <a href=\"${env.BUILD_URL}console\">console output</a>.",
                               status: "<h1><b>*** Failure ***</b></h1>",
                               webhookUrl: env.MS_TEAMS_WEBHOOK_URL
    }

    cleanup {

      sh "docker-compose -f docker-compose.yml down"
      sh "docker image rm --force ${env.DK_TAG}"


    }
  }

}
