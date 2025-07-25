def call(String configPath = 'config/prod.conf') {
    // Load the properties file from the shared library resources
    def configText = libraryResource(configPath)
    def config = readProperties text: configText

    pipeline {
        agent any

        environment {
            SLACK_CHANNEL = config.SLACK_CHANNEL_NAME ?: 'build-status'
            ENVIRONMENT   = config.ENVIRONMENT ?: 'prod'
            CODE_PATH     = config.CODE_BASE_PATH ?: 'env/prod'
            MSG           = config.ACTION_MESSAGE ?: 'Approval Needed'
            KEEP_APPROVAL = config.KEEP_APPROVAL_STAGE ?: 'true'
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
                    echo "Slack Notification to ${SLACK_CHANNEL}"
                }
            }
        }

        post {
            success {
                echo "Grafana Deployed Successfully"
            }
            failure {
                echo "Deployment Failed"
            }
        }
    }
}
