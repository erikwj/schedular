package services


import play.api.libs.concurrent.Akka
import play.api.Play.current
import play.api.libs.mailer._
import org.apache.commons.mail.EmailAttachment
import scala.concurrent.duration._

import akka.actor._
import akka.actor.SupervisorStrategy._
import akka.actor.ActorDSL._
import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging
import akka.routing.SmallestMailboxRoutingLogic
import akka.routing.{ ActorRefRoutee, RoundRobinRoutingLogic, Router }

import java.io.File
import play.api.libs.mailer._

/**
 * Email service
 */
object EmailService {

  /**
   * public interface to send out emails that dispatch the message to the listening actors
   * @param emailMessage the email message
   */

   def mailmessage(subject:String, to: Seq[String], txt: Option[String], attachments: Seq[AttachmentFile]) = Email(
      "Your requested report from VCS " + subject,
      "LunaBot FROM <reports@lunatech.com>",
      to,
      // adds attachment
      attachments = attachments
      /*Seq(
        AttachmentFile(attachmentName, new File(attachmentPath))
        // adds inline attachment from byte array
      )*/,
        // sends text, HTML or both...
        bodyText = txt
        // bodyHtml = html 
      )


  def send(email:Email) = MailerPlugin.send(email)//emailServiceActor ! emailMessage


}