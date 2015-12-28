package services

import com.redis._
// import serialization._
// import Parse.Implicits._

import play.api.libs.concurrent.Akka
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import play.api.Play
import play.api.Play.current
import actors.ReportSender._
import models._
import java.util.Date
import repositories._

class BackupService(repo: BackupRepository) {
  
  private val JOBSKEY = "jobids"

  def jobIds:List[String] = repo.jobIds//redis.lrange(JOBSKEY,0,-1).getOrElse(List[Option[String]]()).flatten

  def get(id:String):Option[String] = repo.get(id)

  def get(id:String,field:String):Option[String] = repo.getField(id,field)
   
  def remove(id: JobId):Unit = repo.remove(id)

  def addScheduledReport(id:JobId, report: ScheduledReport) = repo.addScheduledReport(id,report)

  def addNextRun(id:JobId, nextRun: Long):Boolean = repo.addNextRun(id,nextRun)

  def nextRuns(ids:List[JobId]): List[Date] = repo.nextRuns(ids)

  def scheduled = repo.scheduled 

  def add(id:String,value:play.api.libs.json.JsValue):Option[Int] = repo.add(id,value)

}


