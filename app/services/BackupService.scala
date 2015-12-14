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

  def addScheduledReport(id:String, reportName: String, url: String, schedule: String) = {
    redis.hmset(id,Map("reportName" -> reportName, "url" -> url, "schedule" -> schedule))
  }
  
  def addNextRun(id:String, nextRun: Long) = {
    val result = redis.hmset(id,Map("nextRun" -> nextRun))
    println("addNextRun " + id)
    println(result)
    result
  }



/*
  def nextRuns: List[Date] = {
    redis.pipeline { p =>
      jobIds map {
        id => p.get(id,"nextRun")
      }
    }
  }
*/
  def nextRuns(ids:List[String]): List[Date] = (ids map { id => {
    println("nextRuns " + id)
    val r = redis.hmget("jobId-" + id,"nextRun").getOrElse(Map[String,String]())
    println(r)
    r.get("nextRun") map { s => new Date()}
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


