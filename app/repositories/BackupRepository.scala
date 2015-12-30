package repositories

import models._
import java.util.Date

trait BackupRepository {
  def jobIds:List[String]
  def get(id:String):Option[String] 
  def getField(id:String,field:String):Option[String]
  def remove(id: JobId):Unit 
  def addScheduledReport(id:JobId, report: ScheduledReport): Boolean
  def addNextRun(id:JobId, nextRun: Long):Boolean 
  def nextRuns(ids:List[JobId]): List[Date]
  def scheduled: List[(String, Map[String,String])]
  def add(id:String,value:play.api.libs.json.JsValue):Option[Int]
}

