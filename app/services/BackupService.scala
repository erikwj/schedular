package services

import com.redis._
import play.api.libs.concurrent.Akka
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import play.api.Play
import play.api.Play.current
import actors.ReportSender._

object BackupService extends RedisService {
  
  private val JOBSKEY = "jobids"

  def jobIds = redis.lrange(JOBSKEY,0,-1).getOrElse(List[Option[String]]()).flatten

  def get(id:String):Option[String] = redis.get(id)
   
  def jobId(id:String):String = "jobId-" + id

  def remove(id: String):Unit = {
    //remove the key-value pair
    redis.del(id)
    //remove the id from the list of active jobids
    redis.lrem(JOBSKEY,0,id)
  }

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


