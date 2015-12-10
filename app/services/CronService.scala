package services

import java.util.Date
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
   * description
   * @param para
   */
   // def add(date:Date):Date = ???

   // def nextDate(cron: Cron): Cron

   /*


   1,2,3,4,5,6

  val dates = sd.sliding(2).toList
  val startDate = new Date()
  val date = new SimpleDateFormat("yyyy-MM-dd HH:mm.ss").parse("2015-12-07 23:44.13")
   dates dropWhile { dd => (date).before(dd(0)) }
  */
  
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


  sealed trait CronSchedules
  case object DAILY extends CronSchedules
  case object WEEKLY extends CronSchedules
  case object MONTHLY extends CronSchedules
  // case object ANNUALLY extends CronSchedules
  // case object QUARTERLY extends CronSchedules

  def toCron(startDate:Date,scheme:CronSchedules):String = {
    val dweek = startDate.getDay
    val dmonth = startDate.getMonth
    val dyear = startDate.getYear
    val hours = startDate.getHours
    val minutes = startDate.getMinutes
    val seconds = startDate.getSeconds

    scheme match {
      case DAILY => "%d %d %d * * * *".format(seconds,minutes,hours)
      case WEEKLY => "%d %d %d * * %d *".format(seconds,minutes, hours,dweek) 
      case MONTHLY => "%d %d %d %d * * *".format(seconds,minutes, hours,dmonth) 
      // case ANNUALLY => "%d %d %d * *" 
      // case QUARTERLY => "%d %d %d * *" 
    }
    
  }

  def getStartDate(initialStartDate: Date, currentDates:List[Date], millis: Int): Date = {

    val next: Date => Date = (d:Date) => addMillis(d,millis)
    val sorted = currentDates.sortBy(_.getTime())
    val windows = sorted.sliding(2).toList
    
    def go(date: Date,pairs: List[List[Date]]): Date= pairs match {
      case Nil => date
      case d::xs => if( next(d(0)).getTime <= date.getTime && d(1).after(next(date))) date
                    else go(next(d(1)),xs)
    }
    
    currentDates match {
      case Nil => initialStartDate
      case date::Nil => if(date == initialStartDate) next(initialStartDate)
                     else if(next(date).before(initialStartDate)) initialStartDate
                     else if(next(initialStartDate).before(date)) initialStartDate
                     else next(date)
      case x::xs =>  go(initialStartDate,windows) //at least 2 items in list
    }

  }

  // def timeBetween(dates:List[Date]): List[Long] = {
  //   val sorted = dates.sortBy(_.getTime())
  //   val windows = (sorted map {_.getTime}).sliding(2).toList
  //   windows map ( pair => pair(1) - pair(0))
  // }

  def canAdd(target:Date,add: Date, millis:Int) = target.getTime + millis < add.getTime

  //Add milliseconds to a date
  def addMillis(date:Date,millis:Int):Date = new Date(date.getTime + millis)
    
  

}