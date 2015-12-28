package actors

// import akka.japi.Option.Some
import com.typesafe.config.ConfigFactory
import akka.actor._
import scala.concurrent.duration._
import scala.concurrent.Future
import com.ning.http.client.AsyncHttpClientConfig.Builder
import play.api.libs.ws.ning.NingWSClient
import play.api.libs.json._

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.Logging
import akka.pattern.pipe

import play.api.libs.iteratee._


import java.io.File
import java.io.FileOutputStream
import services._
import ReportFetcher._
import ReportSender._

import play.api.libs.mailer._



  object ReportSender {
    case object Send 
  }

  class ReportSender(reportName: String, target:String,receipients:Seq[String],body: String) extends Actor with ActorLogging {
  	 def uuid = java.util.UUID.randomUUID().toString()

    def receive = {
      case Send => val getter = context.actorOf(ReportFetcher.props(target), "fetcher-" + uuid)
        getter ! CreateReport(reportName)
      case file:File => log.info(s"receiving file from")
        val msg = EmailService.mailmessage(reportName, receipients, Some(body), Seq(AttachmentFile(file.getName(),file)))
        EmailService.send(msg)//(msg)// ! ReportSender(reportName, a.getCanonicalPath(),receipients)

      case _ => log.info("NO FILE RECEIVED")
		    val msg = EmailService.mailmessage("error email", Seq("Miss TO <to@email.com>"), Some("A text message"), Seq[AttachmentFile]())
        EmailService.send(msg)

    }
    
  }

