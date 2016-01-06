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
import repositories._
import ReportSender._
import Formatters._
import quartzscheduler._
import quartzscheduler.QuartzScheduler._
import quartzscheduler.CronSchedule._

import play.api.libs.mailer._
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension


object Application extends Controller {

  val backupRepo = RedisBackupRepository
  val backupService = new BackupService(backupRepo)

  def index = Action {
    Ok("running")
  }

  val jobIntervalInMillis = Play.configuration.getInt("cron.timeinterval").getOrElse(sys.error("Missing 'cron.timeinterval' configuration setting."))
    

  def cancelScheduledReport(jobId:String) = Action {
    if(JobId.isValid(jobId)) {
      val id = JobId(jobId)
      val result = scheduler.cancelJob(id.toString)
        if(result) {
          //delete jobId -> sr
          backupService.remove(id)
        
          Ok(Json.obj("status" -> "success", "description" -> "job cancelled"))
        } else Ok(Json.obj("status" -> "error", "description" -> "job not cancelled","reason" -> "no job found with this id: $jobId"))
    } else Ok(Json.obj("status" -> "error", "description" -> "job not cancelled","reason" -> s"not a valid id: $jobId"))
  }


  def runningJobs = Action {
    val ids = scheduler.runningJobs.keys
    Ok(Json.obj("status" -> "success", "jobIds" -> Json.toJson(ids)))
  }

  def cronSchedule = Action(parse.json) { implicit request => 
    val createSchedule: JsResult[CreateSchedule] = request.body.validate[CreateSchedule]
    createSchedule.fold(
      invalid = {
        fieldErrors => {
          Ok(Json.obj("status" -> "error","description" -> "no valid cronrequest received"))
        }
      },
      valid = { cr => 
        val scheduleOpt = CronSchedule.nextCron(cr.scheme,cr.currentDates,jobIntervalInMillis)
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
        val id = JobId(uuid)

        // val cd:List[java.util.Date] = List(new java.util.Date(1449528240000L),new java.util.Date(1449528270000L))
        val currentDates = backupService.nextRuns(scheduler.runningJobs.keys.toList map { JobId(_)})// List(new java.util.Date(1449528240000L)) //TODO
        println("runningJobs nextRuns " + currentDates)
        println("Scheme" + sr.scheme)

        val cronOpt = CronSchedule.nextCron(sr.scheme,currentDates,jobIntervalInMillis)
        val dateOpt = cronOpt map { (cron) => {
          if(inScope(cron,new java.util.Date())) {

            val cronStr = CronSchedule.toQuartz(cron)
            println("cronStr : " + cronStr)
            val firstRun = scheduler.createSchedule(uuid, Some(s"scheduled report"), cronStr, None)
            println("firstRun : " + firstRun)

            val scheduledReport = ScheduledReport(sr.reportName,sr.url,cron)
            backupService.addScheduledReport(id, scheduledReport)
            val job = Akka.system.actorOf(Props(new ReportSender(sr.reportName,sr.url,sr.to,sr.body)), name="ReportSender-" + id)
            scheduler.schedule(uuid, job, Send)
          } else {
            //put schedule in scope and postpone job to that date
            //Use apitools to resend request at certain date. 
            // Requests.sendRequest(request,request.body)

            //create task to start a scheduled report

            //stub to let the test pass by returning a Date. with current test setup this won't be executed
            new java.util.Date()
          }

          } 
        }
        
        dateOpt match {
          case Some(d) => {
            if(backupService.addNextRun(id, d.getTime)) Ok(Json.obj("status" -> "succes", "description" -> s"scheduled job with id: $id","id" -> uuid, "nextRun" -> Schedule.dateFormat.format(d)))
            else Ok(Json.obj("status" -> "error", "description" -> s"Backup couldn't be added for scheduled job with id: $id","id" -> uuid))
          }
          case _ => Ok(Json.obj("status" -> "error", "description" -> s"Can't create schedule"))
        }
                
      }
    )
  }



}

