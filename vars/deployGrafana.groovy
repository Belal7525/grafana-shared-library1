def call(String configPath = 'config/prod.conf') {
    // Load the properties file from the shared library resources
    def configText = libraryResource(configPath)
    def config = readProperties text: configText

    pipeline {
        agent any

    environment {
    ENVIRONMENT        = "${config.ENVIRONMENT ?: 'prod'}"
    CODE_BASE_PATH     = "${config.CODE_BASE_PATH ?: 'env/prod'}"
    ACTION_MESSAGE     = "${config.ACTION_MESSAGE ?: 'Approval Required'}"
    KEEP_APPROVAL_STAGE = "${config.KEEP_APPROVAL_STAGE ?: 'true'}"
    EMAIL_TO           = "${config.EMAIL_TO ?: 'mohammadbelal1803551@gmail.com'}"
}

        }

        stages {
            stage('Clone Repo') {
                steps {
                    git url: 'https://github.com/Belal7525/grafana-shared-library1.git'
                }
            }

            stage('User Approval') {
                when {
                    expression { return KEEP_APPROVAL.toBoolean() }
                }
                steps {
                    timeout(time: 5, unit: 'MINUTES') {
                        input message: "${MSG}"
                    }
                }
            }

            stage('Run Playbook') {
                steps {
                    sh "ansible-playbook ${CODE_PATH}/install-grafana.yml -i ${CODE_PATH}/inventory.ini"
                }
            }

            stage('Notify') {
                steps {
                    emailext subject: "Grafana Deployment Status",
                             body: "Grafana deployment process has completed on environment: ${ENVIRONMENT}. Check Jenkins console for details.",
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
                         body: "Grafana deployment failed in environment: ${ENVIRONMENT}. Please check Jenkins console output.",
                         to: "${EMAIL_TO}"
            }
        }
    }
}
