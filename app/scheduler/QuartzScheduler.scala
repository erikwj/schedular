package quartzscheduler

import play.api.libs.concurrent.Akka
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import play.api.Play
import play.api.Play.current
import actors.ReportSender._

import play.api.libs._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import java.util.Date
import java.util.TimeZone
import java.text.SimpleDateFormat 

import java.util.Calendar
import org.joda.time._


object QuartzScheduler {
  val scheduler = QuartzSchedulerExtension(Akka.system)  
}

sealed trait CronSchedule {
  val startDate: Date
}

case class DAILY(startDate: Date) extends CronSchedule
case class WEEKLY(startDate: Date) extends CronSchedule
case class MONTHLY(startDate: Date) extends CronSchedule
case class YEARLY(startDate: Date) extends CronSchedule
case class ONCE(startDate: Date) extends CronSchedule

object CronSchedule {

  val startDelay = 120000 //Play.configuration.getInt("cron.startdelay").getOrElse(120000)


  val DAILY_ID = "daily"
  val WEEKLY_ID = "weekly"
  val MONTHLY_ID = "monthly"
  val YEARLY_ID = "yearly"
  val ONCE_ID = "once"

  val ds = Json.format[DAILY]
  val ws = Json.format[WEEKLY]
  val ms = Json.format[MONTHLY]
  val ys = Json.format[YEARLY]
  val o = Json.format[ONCE]


  implicit val fmt: Format[CronSchedule] = new Format[CronSchedule] {
    def reads(json: JsValue): JsResult[CronSchedule] = json \ "schedule" match {
      case JsDefined(JsString(DAILY_ID))      ⇒ Json.fromJson[DAILY](json)(ds)
      case JsDefined(JsString(WEEKLY_ID))     ⇒ Json.fromJson[WEEKLY](json)(ws)
      case JsDefined(JsString(MONTHLY_ID))    ⇒ Json.fromJson[MONTHLY](json)(ms)
      case JsDefined(JsString(YEARLY_ID))    ⇒ Json.fromJson[YEARLY](json)(ys)
      case JsDefined(JsString(ONCE_ID))    ⇒ Json.fromJson[ONCE](json)(o)
      case _                       ⇒ JsError(s"Unexpected JSON value $json")
    }

    def writes(cs: CronSchedule): JsValue = cs match {
      case schedule: DAILY     ⇒ (Json.toJson(schedule)(ds)).as[JsObject] + ("schedule" -> Json.toJson(DAILY_ID))
      case schedule: WEEKLY    ⇒ (Json.toJson(schedule)(ws)).as[JsObject] + ("schedule" -> Json.toJson(WEEKLY_ID))
      case schedule: MONTHLY   ⇒ (Json.toJson(schedule)(ms)).as[JsObject] + ("schedule" -> Json.toJson(MONTHLY_ID))
      case schedule: YEARLY    ⇒ (Json.toJson(schedule)(ys)).as[JsObject] + ("schedule" -> Json.toJson(YEARLY_ID))
      case schedule: ONCE    ⇒ (Json.toJson(schedule)(o)).as[JsObject] + ("schedule" -> Json.toJson(ONCE_ID))
    }
  }

  val update = (s:CronSchedule,d:Date) => s match {
    case DAILY(_) => Some(DAILY(d))
    case WEEKLY(_) => Some(WEEKLY(d))
    case MONTHLY(_) => Some(MONTHLY(d))
    case YEARLY(_) => Some(YEARLY(d))
    case ONCE(_) => Some(ONCE(d))
    case _ => None
  }

  /** 
   * Creates as org.quartz.CronExpression see http://quartz-scheduler.org/api/2.2.1/org/quartz/CronExpression.html
   * Not all features of a CronExpression are currently supported
   *
   * second the second definition of the schedule (0-59)
   * minute the minute definition (0-59)
   * hour   the hour definition (0-23)
   * dmonth the day of month definition (1-31)
   * month  the month definition (1-12)
   * dweek  the day of week definition (1-7 )
   * year   the year definition [Optional] 
   * 
   * The UTC timezone is used as this is also the default for the Akka config, but actually it should read from the akka config
   */

  def toQuartz(scheme:CronSchedule):String = {
    val startDate = scheme.startDate
    val calendar = Calendar.getInstance();

    val utc_tz = TimeZone.getTimeZone("UTC");
    calendar.setTimeZone(utc_tz)
    calendar.setTime(startDate)
    val dweek = calendar.get(Calendar.DAY_OF_WEEK)
    val dmonth = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH) 
    val year = calendar.get(Calendar.YEAR) - 1900
    val hours = calendar.get(Calendar.HOUR_OF_DAY) 
    val minutes = calendar.get(Calendar.MINUTE)
    val seconds = calendar.get(Calendar.SECOND)
    val milliseconds = calendar.get(Calendar.MILLISECOND)

    scheme match {
      case DAILY(_) => "%d %d %d * * *".format(seconds,minutes,hours)
      case WEEKLY(_) => "%d %d %d ? * %d".format(seconds,minutes, hours, dweek) 
      case MONTHLY(_) => "%d %d %d %d * ?".format(seconds,minutes, hours, dmonth) 
      case YEARLY(_) => "%d %d %d %d %d ?".format(seconds, minutes, hours, dmonth, month)  
      case ONCE(_) => "%d %d %d %d %d ? %d".format(seconds, minutes, hours, dmonth, month, year)  
    }
    
  }

  def inScope(scheme:CronSchedule, start: Date):Boolean = {
    val now = new DateTime(start)
    scheme match {
      case DAILY(d) => now.plusDays(1).isAfter(new DateTime(d))
      case WEEKLY(d) => now.plusDays(7).isAfter(new DateTime(d))
      case MONTHLY(d) => now.plusMonths(1).isAfter(new DateTime(d))
      case YEARLY(d) => now.plusYears(1).isAfter(new DateTime(d))
      case ONCE(d) => true
    }    
  }

/*  def putInScope(scheme:CronSchedule, start: Date):Boolean = {
    val now = new DateTime(start)
    scheme match {
      case DAILY(d) => now.plusDays(1).isAfter(new DateTime(d))
      case WEEKLY(d) => now.plusDays(7).isAfter(new DateTime(d))
      case MONTHLY(d) => now.plusMonths(1).isAfter(new DateTime(d))
      case YEARLY(d) => now.plusYears(1).isAfter(new DateTime(d))
      case ONCE(d) => true
    }    
  }*/

  def nextCron(scheme: CronSchedule, currentDates:List[Date], millis: Int): Option[CronSchedule] = {
    val updateScheme = (d:Date) => update(scheme,d)
    updateScheme(getStartDate(scheme.startDate,currentDates,millis))
  }

  def getStartDate(initialStartDate: Date, currentDates:List[Date], millis: Int): Date = {
    val next: Date => Date = (d:Date) => addMillis(d,millis)
    val sorted = currentDates.sortBy(_.getTime())
    val windows = sorted.sliding(2).toList
    val now = addMillis(new Date(),startDelay)
    // println("now DATE " + now)
    val isd = if(initialStartDate.before(now)) now else initialStartDate
    // println("ISD DATE " + isd)
    
    def go(date: Date,pairs: List[List[Date]]): Date= pairs match {
      case Nil => date
      case d::xs => if( next(d(0)).getTime <= date.getTime && d(1).after(next(date))) date
                    else go(next(d(1)),xs)
    }
    
    currentDates match {
      case Nil => isd
      case date::Nil => if(date == isd) next(isd)
                     else if(next(date).before(isd)) isd
                     else if(next(isd).before(date)) isd
                     else next(date)
      case x::xs =>  go(isd,windows) //at least 2 items in list
    }

  }

  def canAdd(target:Date,add: Date, millis:Int) = target.getTime + millis < add.getTime

  //Add milliseconds to a date
  def addMillis(date:Date,millis:Int):Date = new Date(date.getTime + millis)
  
/*  def convertLocalTimeToUTC(localTimeZone: String, p_localDateTime: String): Date = {
    //create a new Date object using the timezone of the specified city
    val parser = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
    val utcparser = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
    parser.setTimeZone(TimeZone.getTimeZone(localTimeZone))
    val localDate = parser.parse(p_localDateTime)
    val formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z'('Z')'")
    // formatter.setTimeZone(TimeZone.getTimeZone(localTimeZone))
    // println("convertLocalTimeToUTC: "+localTimeZone+": "+" The Date in the local time zone " +   formatter.format(localDate))

    //Convert the date from the local timezone to UTC timezone
    formatter.setTimeZone(TimeZone.getTimeZone("UTC"))
    val dateFormateInUTC = formatter.parse(formatter.format(localDate))
    dateFormateInUTC
    // formatter.parse(dateFormateInUTC)
  }*/

}