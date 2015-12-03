package controllers

import play.api._
import play.api.mvc._
import scala.concurrent._
import scala.concurrent.duration._
import play.api.libs._
import play.api.libs.json._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.Logging
import akka.pattern.pipe

import java.io.File
import java.io.FileOutputStream

import models._
import actors._
import services._
import ReportSender._
import Formatters._
import utils.SchedulerUtil._

import play.api.libs.mailer._

import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension


object Application extends Controller {

  def index = Action {
    Ok("running")
  }

  def cancelScheduledReport(jobId:String) = Action {
    if(JobId.isValid(jobId)) {
      val id = BackupService.jobId(jobId)
      val result = scheduler.cancelJob(id)
        if(result) {
          //delete jobId -> sr
          BackupService.remove(id)
        
          Ok(Json.obj("result" -> "job cancelled"))
        } else Ok(Json.obj("error" -> "job not cancelled","reason" -> "no job found with this id"))
    } else Ok(Json.obj("error" -> "job not cancelled","reason" -> "not a valid id"))
  }

  // def resumeScheduler = ???

  def scheduleReport = Action(parse.json) { implicit request => 
    val mailrequest: JsResult[ScheduleReportToBeSent] = request.body.validate[ScheduleReportToBeSent]
    mailrequest.fold(
      invalid = {
        fieldErrors => {
          val errorMessage = EmailService.mailmessage("Error message", Seq("LunaTech Error report TO <to@email.com>"), Some("There was an error processing a request: $request"), Seq[AttachmentFile]())
          val result = EmailService.send(errorMessage)
          Ok(Json.obj("Error" -> "No valid request received"))
        }
      },
      valid = { sr => 
        val uuid = java.util.UUID.randomUUID().toString()
        val id = BackupService.jobId(uuid)

        //save jobId -> sr
        BackupService.add(id, request.body)
        val report = Akka.system.actorOf(Props(new ReportSender(sr.reportName,sr.url,sr.to,sr.body)), name="ReportSender-" + id)

        scheduler.createSchedule(id, Some(s"scheduled report $id"), sr.schedule, None)
        val jobDt = scheduler.schedule(id, report, Send)

        Ok(Json.obj("Description" -> s"Scheduled job with $id","id" -> uuid, "nextRun" -> Schedule.dateFormat.format(jobDt), "request" -> request.body))
    
      }
    )
  }


  def ReportSender(subject: String, body: String, to: Seq[String], reportName:String, url:String) = {
    val report = Akka.system.actorOf(Props(new ReportSender(reportName,url,to,body)), name="report")
    report ! Send
  }

}

