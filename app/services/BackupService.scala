package services

import com.redis._
import play.api.libs.concurrent.Akka
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import play.api.Play.current
import actors.ReportSender._

object BackupService {
  
   val redisServer = "localhost"
   val redisPort = 6379

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
        redis.lpush("jobids",id)
    }
}


