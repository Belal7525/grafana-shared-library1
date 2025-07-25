def call(String configPath = 'config/prod.conf') {
    def config = readProperties file: "${libraryResource(configPath)}"

    pipeline {
        agent any

        environment {
            SLACK_CHANNEL = "${config.SLACK_CHANNEL_NAME}"
            ENVIRONMENT = "${config.ENVIRONMENT}"
            CODE_PATH = "${config.CODE_BASE_PATH}"
            MSG = "${config.ACTION_MESSAGE}"
            KEEP_APPROVAL = "${config.KEEP_APPROVAL_STAGE}"
        }

        stages {
            stage('🧬 Clone Repo') {
                steps {
                    git url: 'https://github.com/your-org/ansible-grafana-setup.git'
                }
            }

            stage('⏳ User Approval') {
                when {
                    expression { return KEEP_APPROVAL.toBoolean() }
                }
                steps {
                    timeout(time: 5, unit: 'MINUTES') {
                        input message: "Approval required: ${MSG}"
                    }
                }
            }

            stage('📦 Run Playbook') {
                steps {
                    sh "ansible-playbook ${CODE_PATH}/install-grafana.yml -i ${CODE_PATH}/inventory.ini"
                }
            }

            stage('📢 Notify') {
                steps {
                    echo "Slack Notification to ${SLACK_CHANNEL}"
                }
            }
        }

        post {
            success {
                echo "✅ Grafana Deployed Successfully"
            }
            failure {
                echo "❌ Deployment Failed"
            }
        }
    }
}
