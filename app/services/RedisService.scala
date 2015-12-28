package services

import com.redis._
import play.api.libs.concurrent.Akka
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import play.api.Play
import play.api.Play.current
import actors.ReportSender._

trait RedisService {
  
  // val redisServer = Play.configuration.getString("redis.url").getOrElse(sys.error("Missing 'redis.url' configuration setting."))
  // val redisPort = Play.configuration.getInt("redis.port").getOrElse(sys.error("Missing 'redis.port' configuration setting."))
  // val redis = new RedisClient(redisServer, redisPort)
}