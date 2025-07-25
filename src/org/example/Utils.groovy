package org.example

class Utils implements Serializable {
    def steps
    Utils(steps) {
        this.steps = steps
    }

    def notifySlack(String channel, String message) {
        steps.echo "Slack message to ${channel}: ${message}"
        // Use slackSend here if Slack plugin is configured
        // steps.slackSend(channel: channel, message: message)
    }
}
