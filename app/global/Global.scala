package global 

import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Props
import scala.concurrent.Future

import actors._
import models._
import Formatters._
import ReportSender._
import services.BackupService
import services.CronService
import utils.SchedulerUtil._

object Global extends GlobalSettings {


  override def onStart(app: Application) {

    Logger.info("Check if Redis is running")
    //Todo

    Logger.info("Load schedule data")
    val keys:List[String] = BackupService.jobIds 

    def load(id:String, sr:ScheduleReportToBeSent):Unit = {
        val report = Akka.system.actorOf(Props(new ReportSender(sr.reportName,sr.url,sr.to,sr.body)), name="ReportSender-" + id)
        scheduler.createSchedule(id, Some(s"scheduled report $id"), CronService.toQuartz(sr.scheme), None)
        val nextRun = scheduler.schedule(id, report, Send)
        Logger.info("Loading schedule %s with id: %s scheduled for %s".format(sr.reportName,id, Schedule.dateFormat.format(nextRun)))
    }
    
    for {
      id <- keys
      jsonString <- BackupService.get(id) 
      scheduledreport <- Json.parse(jsonString).asOpt[ScheduleReportToBeSent]
      _ <- Option(load(id,scheduledreport))
    } yield ()


    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }
  
  // override def onError(request: RequestHeader, ex: Throwable) = {
  // Future.successful(InternalServerError(
  //   views.html.errorPage(new PlayException ("error","description",ex))
  // ))
  // }

}