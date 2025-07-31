pipeline {
    agent any

    environment {
        ENVIRONMENT         = 'prod'
        CODE_BASE_PATH      = 'env/prod'
        ACTION_MESSAGE      = 'Approval Required'
        KEEP_APPROVAL_STAGE = 'true'
        EMAIL_TO            = 'mohammadbelal1803551@gmail.com'
        ANSIBLE_HOST_KEY_CHECKING = 'False'  // disables strict host key checking
    }

    stages {
        stage('Clone Repo') {
            steps {
                git branch: 'main', url: 'https://github.com/Belal7525/grafana-shared-library1.git'
            }
        }

        stage('User Approval') {
            when {
                expression { return env.KEEP_APPROVAL_STAGE == 'true' }
            }
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    input message: "${env.ACTION_MESSAGE}"
                }
            }
        }

        stage('Run Playbook') {
            steps {
                // Use Jenkins credential with ID 'grafana-key' for SSH
                sshagent(['grafana-key']) {
                    sh "ansible-playbook ${env.CODE_BASE_PATH}/install-grafana.yml -i ${env.CODE_BASE_PATH}/inventory.ini"
                }
            }
        }

        stage('Notify') {
            steps {
                emailext subject: "Grafana Deployment Status",
                         body: "Grafana deployment completed on environment: ${env.ENVIRONMENT}.",
                         to: "${env.EMAIL_TO}"
            }
        }
    }

    post {
        success {
            emailext subject: "Grafana Deployed Successfully",
                     body: "Grafana deployment was successful in environment: ${env.ENVIRONMENT}.",
                     to: "${env.EMAIL_TO}"
        }
        failure {
            emailext subject: "Grafana Deployment Failed",
                     body: "Grafana deployment failed in environment: ${env.ENVIRONMENT}. Please check Jenkins console output.",
                     to: "${env.EMAIL_TO}"
        }
    }
}
