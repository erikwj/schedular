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

object BackupService extends RedisService {
  
  private val JOBSKEY = "jobids"

  def jobIds:List[String] = redis.lrange(JOBSKEY,0,-1).getOrElse(List[Option[String]]()).flatten

  def get(id:String):Option[String] = redis.get(id)

  def get(id:String,field:String):Option[String] = redis.hget(id,field)
   
  def jobId(id:String):String = "jobId-" + id

  def remove(id: String):Unit = {
    //remove the key-value pair
    redis.del(id)
    //remove the id from the list of active jobids
    redis.lrem(JOBSKEY,0,id)
  }

  def addScheduledReport(id:String, sr: ScheduleReportToBeSent) = {
    redis.hmset(id,Map("reportName" -> sr.reportName, "url" -> sr.url, "schedule" -> sr.schedule))
  }
  
  // def addNextRun(id:String, nextRun: Long) = {
  //   redis.hmset(id,Map("nextRun" -> nextRun))
  // }


/*
val dates = sd.sliding(2).toList
val startDate = new Date()
val date = new SimpleDateFormat("yyyy-MM-dd HH:mm.ss").parse("2015-12-07 23:44.13")
 dates dropWhile { dd => (date).before(dd(0)) }



*/
  def nextRuns: List[String] = (jobIds map { id => {
    val a = redis.get(id,"nextRun")
    println(a)
    a
    }}).flatten

  def scheduled = jobIds map { id => redis.hmget[String,String](id,"reportName", "nextRun")}

  def add(id:String,value:play.api.libs.json.JsValue):Option[Int] = {
    //add the key value pair
    val success = redis.set(id, value)
    //add the id to the list of active jobids
    if(success) {
      redis.lpush(JOBSKEY,id) map {_.toInt}
    }
    else None
  }
}


