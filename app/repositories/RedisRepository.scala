package repositories

import com.redis._
import play.api.libs.concurrent.Akka
import play.api.Play
import play.api.Play.current
import actors.ReportSender._

trait RedisRepository {
  
  val redisServer = Play.configuration.getString("redis.url").getOrElse(sys.error("Missing 'redis.url' configuration setting."))
  val redisPort = Play.configuration.getInt("redis.port").getOrElse(sys.error("Missing 'redis.port' configuration setting."))
  val redis = new RedisClient(redisServer, redisPort)
}