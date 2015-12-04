package services

import com.redis._
import play.api.libs.concurrent.Akka
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import play.api.Play
import play.api.Play.current
import actors.ReportSender._

object BackupService {
  
  private val redisServer = Play.configuration.getString("redis.url").getOrElse(sys.error("Missing 'redis.url' configuration setting."))
  private val redisPort = Play.configuration.getInt("redis.port").getOrElse(sys.error("Missing 'redis.port' configuration setting."))
  private val redis = new RedisClient(redisServer, redisPort)
  private val JOBSKEY = "jobids"

  def jobIds = redis.lrange(JOBSKEY,0,-1).getOrElse(List[Option[String]]()).flatten

  def get(id:String) = redis.get(id)
   
  def jobId(id:String) = "jobId-" + id

  def remove(id: String) = {
    redis.del(id)
        redis.lrem(JOBSKEY,0,id)
    }

  def add(id:String,value:play.api.libs.json.JsValue) = {
    redis.set(id, value)
        redis.lpush(JOBSKEY,id)
    }
}


