package global 

import play.api._
import utils.SchedulerUtil._
import play.api.libs.json._

// import scala.concurrent.ExecutionContext.Implicits.global
// import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Props
import actors._
import models._
import Formatters._
import ReportSender._
import services.BackupService

object Global extends GlobalSettings {


  override def onStart(app: Application) {

  	Logger.info("Check if Redis is running")
  	//Todo

  	Logger.info("Load schedule data")
  	val keys:List[String] = BackupService.jobIds 

  	// val schedules:List[String] = (keys map { (id) => BackupService.get(id) }).flatten
  	// val jsonSchedules:List[JsValue] = schedules map { json => Json.parse(json)}

  	def load(id:String, sr:ScheduleReportToBeSent) = {
        val report = Akka.system.actorOf(Props(new ReportSender(sr.reportName,sr.url,sr.to,sr.body)), name="ReportSender-" + id)
        scheduler.createSchedule(id, Some(s"scheduled report $id"), sr.schedule, None)
        scheduler.schedule(id, report, Send)
  	}
  	
  	val loadedSchedules:List[(String,java.util.Date)] = for {
  		id <- keys
  		jsonString <- BackupService.get(id) 
  		scheduledreport <- Json.parse(jsonString).asOpt[ScheduleReportToBeSent]
  	} yield (id,load(id,scheduledreport))

    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}