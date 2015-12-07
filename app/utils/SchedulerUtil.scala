package utils

import com.redis._
import play.api.libs.concurrent.Akka
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import play.api.Play.current
import actors.ReportSender._

object SchedulerUtil {
  
	val scheduler = QuartzSchedulerExtension(Akka.system)

	/**
	 * Represents a cron schedule definition. See [[http://en.wikipedia.org/wiki/Cron]] for a definition of the parameters.
	 *
	 * @constructor create a specific schedule
	 * @param second the second definition of the schedule
	 * @param minute the minute definition
	 * @param hour   the hour definition
	 * @param dmonth the day of month definition
	 * @param month  the month definition
	 * @param dweek  the day of week definition
	 * @param year   the year definition
	 */
	case class Cron (second: String,
	                 minute: String,
	                 hour: String,
	                 dmonth: String,
	                 month: String,
	                 dweek: String,
	                 year: String) {

	}

	object Cron {
		
	}

  
}