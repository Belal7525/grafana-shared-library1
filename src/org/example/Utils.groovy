package org.example

class Utils implements Serializable {
    def steps
    Utils(steps) {
        this.steps = steps
    }

    def notifyEmail(String subject, String message) {
        def recipient = "mohammadbelal1803551@gmail.com"
        steps.echo "Sending email to ${recipient} with subject: ${subject}"
        steps.mail(
            to: recipient,
            subject: subject,
            body: message
        )
    }
}
