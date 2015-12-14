package services

import java.util.Date
import java.util.Calendar
import models._
import org.joda.time._
/**
 * Time service for cron jobs
 */
object CronService {

  final val INTERVAL = 6
  final val START_REPORTING = 3
  final val END_REPORTING = 7
  final val DAILY_START = 0
  final val WEEKLY_START = 1
  final val MONTHLY_START = 3
  
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

  def toQuartz(scheme:CronSchedule):String = {
    val startDate = scheme.startDate
    val calendar = Calendar.getInstance();
    calendar.setTime(startDate)
    val dweek = calendar.get(Calendar.DAY_OF_WEEK); 
    val dmonth = startDate.getDate
    val month = startDate.getMonth
    val year = startDate.getYear
    val hours = startDate.getHours
    val minutes = startDate.getMinutes
    val seconds = startDate.getSeconds

    scheme match {
      // "*/30 * * ? * *"
      case DAILY(_) => "%d %d %d * * *".format(seconds,minutes,hours)
      case WEEKLY(_) => "%d %d %d ? * %d".format(seconds,minutes, hours, dweek) 
      case MONTHLY(_) => "%d %d %d %d * ?".format(seconds,minutes, hours, dmonth) 
      case YEARLY(_) => "%d %d %d %d %d ?".format(seconds, minutes, hours, dmonth, month)  
      case ONCE(_) => "%d %d %d %d %d %d".format(seconds, minutes, hours, dmonth, month, year)  
    }
    
  }

  def cron(scheme: CronSchedule, currentDates:List[Date], millis: Int): Option[CronSchedule] = {
    val startDate = getStartDate(scheme.startDate,currentDates,millis)
    println("START DATE " + startDate)
    CronSchedule.update(scheme,startDate)
  }

  def getStartDate(initialStartDate: Date, currentDates:List[Date], millis: Int): Date = {
    println("initialStartDate DATE " + initialStartDate)
  
    val next: Date => Date = (d:Date) => addMillis(d,millis)
    val sorted = currentDates.sortBy(_.getTime())
    val windows = sorted.sliding(2).toList
    val now = new Date()
    val isd = if(initialStartDate.before(now)) now else initialStartDate
    
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
    
  

}