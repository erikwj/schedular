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
        
          Ok(Json.obj("status" -> "success", "description" -> "job cancelled"))
        } else Ok(Json.obj("status" -> "error", "description" -> "job not cancelled","reason" -> "no job found with this id: $jobId"))
    } else Ok(Json.obj("status" -> "error", "description" -> "job not cancelled","reason" -> s"not a valid id: $jobId"))
  }

  def runningJobs = Action {
    val ids = scheduler.runningJobs.keys
            println(BackupService.nextRuns)

    // println(ids)
    Ok(Json.obj("status" -> "success", "jobIds" -> Json.toJson(ids)))
  }
  // def resumeScheduler = ???

  def scheduleReport = Action(parse.json) { implicit request => 
    val mailrequest: JsResult[ScheduleReportToBeSent] = request.body.validate[ScheduleReportToBeSent]
    mailrequest.fold(
      invalid = {
        fieldErrors => {
          val errorMessage = EmailService.mailmessage("Error message", Seq("LunaTech Error report TO <to@email.com>"), Some("There was an error processing a request: $request"), Seq[AttachmentFile]())
          val result = EmailService.send(errorMessage)
          Ok(Json.obj("status" -> "error","description" -> "no valid request received"))
        }
      },
      valid = { sr => 
        val uuid = java.util.UUID.randomUUID().toString()
        val id = BackupService.jobId(uuid)

        //save jobId -> sr
        BackupService.add(id, request.body)
        // BackupService.addScheduledReport(id, sr)
        val report = Akka.system.actorOf(Props(new ReportSender(sr.reportName,sr.url,sr.to,sr.body)), name="ReportSender-" + id)

        scheduler.createSchedule(id, Some(s"scheduled report $id"), sr.schedule, None)
        val jobDt = scheduler.schedule(id, report, Send)
        // BackupService.addNextRun(id, jobDt.getTime())

        // TimeService.add(jobDt)
        Ok(Json.obj("status" -> "succes", "description" -> s"scheduled job with id: $id","id" -> uuid, "nextRun" -> Schedule.dateFormat.format(jobDt)))
    
      }
    )
  }



    // def load(id:String, sr:ScheduleReportToBeSent):Date = {
    //     val report = Akka.system.actorOf(Props(new ReportSender(sr.reportName,sr.url,sr.to,sr.body)), name="ReportSender-" + id)
    //     scheduler.createSchedule(id, Some(s"scheduled report $id"), sr.schedule, None)
    //     scheduler.schedule(id, report, Send)
    // }
  // def ReportSender(subject: String, body: String, to: Seq[String], reportName:String, url:String) = {
  //   val report = Akka.system.actorOf(Props(new ReportSender(reportName,url,to,body)), name="report")
  //   report ! Send
  // }

}

