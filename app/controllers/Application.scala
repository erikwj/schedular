package controllers

import play.api._
import play.api.mvc._
import scala.concurrent._
import scala.concurrent.duration._
import play.api.libs._
import play.api.libs.json._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.Logging
import akka.pattern.pipe
// import akka.testkit.TestProbe

import play.api.libs.iteratee._


import java.io.File
import java.io.FileOutputStream

import models._
import services._
import ReportService._
import ScheduledReport._

import play.api.libs.mailer._

import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension


object Application extends Controller {
  
  def index = Action {
    Ok("running")
  }

  val system = ActorSystem("Scheduler")

  val scheduler = QuartzSchedulerExtension(system)

  case class NewProbe(probe: ActorRef)
  case object Tick
  case object Tock

  class ScheduleTestReceiver extends Actor with ActorLogging {
    var probe: ActorRef = _
    def receive = {
      case NewProbe(_p) =>
        probe = _p
      case Tick =>
        log.info("Got a Tick.")
        // probe ! Tock
    }
  }

  val receiver = system.actorOf(Props(new ScheduleTestReceiver))
  class TestProbe extends Actor with ActorLogging  {

    def receive = {
      case a:Any => log.info("Got a Tick.")
    }
    
  }


  
  def sendOnce = Action(parse.json) { implicit request => 
      val mailrequest: JsResult[SendReportRequest] = request.body.validate[SendReportRequest]
      mailrequest.fold(
        invalid = {
          fieldErrors => {
            val errorMessage = EmailService.mailmessage("Error message", Seq("LunaTech Error report TO <to@email.com>"), Some("There was an error processing a request: $request"), Seq[AttachmentFile]())
            val result = EmailService.send(errorMessage)
            Ok(Json.obj("Error" -> "No valid request received"))
          }
          },
        valid = { mailrequest => 
          val uuid =  java.util.UUID.randomUUID().toString()

          QuartzSchedulerExtension(system).createSchedule(uuid, Some("Creating new dynamic schedule"), mailrequest.schedule, None)

          val report = system.actorOf(Props(new ScheduledReport(mailrequest.reportName,mailrequest.url,mailrequest.to,mailrequest.body)), name="report-" + uuid)

          val jobDt = scheduler.schedule(uuid, report, Create)


          // val jobDt = QuartzSchedulerExtension(system).schedule("cronEvery30Seconds", receiver, Tick)
  
          // val result = sendReport(mailrequest.subject, mailrequest.body, mailrequest.to, mailrequest.reportName,mailrequest.url)    
          Ok(Json.obj("Action" -> s"Sending request to email for schedule $uuid on "))
      
        }
      )
  }


  def sendReport(subject: String, body: String, to: Seq[String], reportName:String, url:String) = {
    val _system = ActorSystem("ReportingSystem")
    val report = _system.actorOf(Props(new ScheduledReport(reportName,url,to,body)), name="report")

    report ! Create
  }

}

