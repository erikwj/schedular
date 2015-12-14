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
import CronSchedule._
import Formatters._
import utils.SchedulerUtil._

import play.api.libs.mailer._
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension


object Application extends Controller {

  def index = Action {
    Ok("running")
  }

  val millis = Play.configuration.getInt("cron.timeinterval").getOrElse(sys.error("Missing 'cron.timeinterval' configuration setting."))
    

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
    Ok(Json.obj("status" -> "success", "jobIds" -> Json.toJson(ids)))
  }

  def cronSchedule = Action(parse.json) { implicit request => 
    val cronrequest: JsResult[CreateSchedule] = request.body.validate[CreateSchedule]
    cronrequest.fold(
      invalid = {
        fieldErrors => {
          Ok(Json.obj("status" -> "error","description" -> "no valid cronrequest received"))
        }
      },
      valid = { cr => 
        val scheduleOpt = CronService.cron(cr.scheme,cr.currentDates,millis)
        scheduleOpt match {
          case Some(cs) => Ok(Json.obj("status" -> "success", "cron" -> cs))
          case None => Ok(Json.obj("status" -> "error", "reason" -> "Couldn't create cron string"))
        }
      }
    )

  }

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
        // BackupService.add(id, request.body)
        val report = Akka.system.actorOf(Props(new ReportSender(sr.reportName,sr.url,sr.to,sr.body)), name="ReportSender-" + id)
        
        // val cd:List[java.util.Date] = List(new java.util.Date(1449528240000L),new java.util.Date(1449528270000L))
        val currentDates = BackupService.nextRuns(scheduler.runningJobs.keys.toList)// List(new java.util.Date(1449528240000L)) //TODO
        println("runningJobs nextRuns " + currentDates)
        // println("CurrentDates " + cd)
        println("Scheme" + sr.scheme)
        val cronOpt = CronService.cron(sr.scheme,currentDates,millis)
        val dateOpt = cronOpt map { (cron) => {
          val cronStr = CronService.toQuartz(cron)
          scheduler.createSchedule(uuid, Some(s"scheduled report"), cronStr, None)
          // case class ScheduleReportToBeSent(subject: String, body: String, to: Seq[String],reportName:String,  url: String, scheme: CronSchedule)

          // val srtbs = ScheduleReportToBeSent(sr.subject,sr.body,sr.to,sr.reportName,sr.url,cron)
          // BackupService.addScheduledReport(id, srtbs)
          scheduler.schedule(uuid, report, Send)
          } 
        }
        
        dateOpt match {
          case Some(d) => {
            BackupService.addNextRun(id, d.getTime)
            Ok(Json.obj("status" -> "succes", "description" -> s"scheduled job with id: $id","id" -> uuid, "nextRun" -> Schedule.dateFormat.format(d)))
          }
          case _ => Ok(Json.obj("status" -> "error", "description" -> s"Can't create schedule"))
        }
                
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

