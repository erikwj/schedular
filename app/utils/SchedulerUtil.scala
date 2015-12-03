package utils

import com.redis._
import play.api.libs.concurrent.Akka
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import play.api.Play.current
import actors.ReportSender._

object SchedulerUtil {
  
	val scheduler = QuartzSchedulerExtension(Akka.system)
  
}