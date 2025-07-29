def call(String configPath = 'config/prod.conf') {
    // Load config file
    def configText = libraryResource(configPath)
    def config = readProperties text: configText

    pipeline {
        agent any

        environment {
            ENVIRONMENT         = "${config.ENVIRONMENT ?: 'prod'}"
            CODE_BASE_PATH      = "${config.CODE_BASE_PATH ?: 'env/prod'}"
            ACTION_MESSAGE      = "${config.ACTION_MESSAGE ?: 'Approval Required'}"
            KEEP_APPROVAL_STAGE = "${config.KEEP_APPROVAL_STAGE ?: 'true'}"
            EMAIL_TO            = "${config.EMAIL_TO ?: 'mohammadbelal1803551@gmail.com'}"
        }

        stages {
            stage('Clone Repo') {
                steps {
                    git branch: 'main', url: 'https://github.com/Belal7525/grafana-ansible-playbooks.git'
                }
            }

            stage('User Approval') {
                when {
                    expression { return KEEP_APPROVAL_STAGE.toBoolean() }
                }
                steps {
                    timeout(time: 5, unit: 'MINUTES') {
                        input message: "${ACTION_MESSAGE}"
                    }
                }
            }

            stage('Run Playbook') {
                steps {
                    sh "ansible-playbook ${CODE_BASE_PATH}/install-grafana.yml -i ${CODE_BASE_PATH}/inventory.ini"
                }
            }

            stage('Notify') {
                steps {
                    emailext subject: "Grafana Deployment Status",
                             body: "Grafana deployment has completed on environment: ${ENVIRONMENT}.",
                             to: "${EMAIL_TO}"
                }
            }
        }

        post {
            success {
                emailext subject: "Grafana Deployed Successfully",
                         body: "Grafana deployment was successful in environment: ${ENVIRONMENT}.",
                         to: "${EMAIL_TO}"
            }
            failure {
                emailext subject: "Grafana Deployment Failed",
                         body: "Grafana deployment failed in environment: ${ENVIRONMENT}. Check Jenkins logs.",
                         to: "${EMAIL_TO}"
            }
        }
    }
}
