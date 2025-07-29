pipeline {
    agent any

    stages {
        stage('Init') {
            steps {
                script {
                    // Safe dynamic assignments
                    env.ENVIRONMENT = config.ENVIRONMENT ?: 'prod'
                    env.CODE_BASE_PATH = config.CODE_BASE_PATH ?: 'env/prod'
                    env.ACTION_MESSAGE = config.ACTION_MESSAGE ?: 'Approved'
                    env.KEEP_APPROVAL_STAGE = config.KEEP_APPROVAL_STAGE ?: 'true'
                    env.EMAIL_TO = config.EMAIL_TO ?: 'mohammadbelal1803551@gmail.com'
                }
            }
        }

        stage('Print Config') {
            steps {
                echo "Environment: ${env.ENVIRONMENT}"
                echo "Base Path: ${env.CODE_BASE_PATH}"
                echo "Email To: ${env.EMAIL_TO}"
            }
        }
    }
}
