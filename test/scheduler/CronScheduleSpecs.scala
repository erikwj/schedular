package test.scheduler

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._

import java.text.SimpleDateFormat
import java.util.Date


import quartzscheduler._
import quartzscheduler.QuartzScheduler._
import quartzscheduler.CronSchedule._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class CronScheduleSpec extends Specification {

  val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  val s = format.parse("2016-12-29T23:44:00.000+0100")
  val s2 = format.parse("2016-12-29T23:44:05.000+0100")
  val s3 = format.parse("2016-12-29T23:44:03.900+0100")
  val d1 = format.parse("2016-12-29T23:14:00.000+0100")
  val d2 = format.parse("2016-12-29T23:44:01.000+0100")
  val d3 = format.parse("2016-12-29T23:44:02.000+0100")
  val d4 = format.parse("2016-12-29T23:44:03.000+0100")
  val d5 = format.parse("2016-12-29T23:44:04.000+0100")
  val d6 = format.parse("2016-12-29T23:44:15.000+0100")
  
  "CronSchedule" should {

    "have a method that returns the next available timeslot where" in {
          val dates = List(d1,d2,d3,d4,d5,d6)
          val newDate = getStartDate(s,dates,1000) 
          val newDate3 = getStartDate(s3,dates,1000) 
          val newDate2 = getStartDate(s,List[Date](),2000) 
          newDate must_== s2 
      "a new unique date is returned" in {
          newDate3 must_== s2 
          newDate2 must_== s
      }

    }
    

    "have a toJson method" in {
      val date = new SimpleDateFormat("yyyy-MM-dd HH:mm.ss").parse("2015-12-07 23:44.00")
      val weekly = WEEKLY(date)
      Json.toJson(weekly) must_== Json.parse("""{"schedule":"weekly", "startDate":1449528240000}""")    
    }

   "be able to generate cronschedule from startdate" in {
    toQuartz(DAILY(s2)) must_== "5 44 22 * * *"
    toQuartz(WEEKLY(s2)) must_== "5 44 22 ? * 5"
    toQuartz(MONTHLY(s2)) must_== "5 44 22 29 * ?"
    toQuartz(YEARLY(s2)) must_== "5 44 22 29 11 ?"
    toQuartz(ONCE(s2)) must_== "5 44 22 29 11 ? 116"
   }

   "be able to distinguish if a Schedule is in scope compared to now" in {
    val nowExact = format.parse("2015-12-22T23:44:00.000+0100")
    val nowInScope = format.parse("2015-12-22T23:45:00.000+0100")
    val nextMonth = format.parse("2016-01-22T23:44:00.000+0100")
    val nextYear = format.parse("2016-12-22T23:44:00.000+0100")
    val nextDay = format.parse("2015-12-23T23:44:00.000+0100")
    val nextWeek = format.parse("2015-12-29T23:44:00.000+0100")

    inScope(DAILY(nextDay),nowExact) must beFalse
    inScope(DAILY(nextDay),nowInScope) must beTrue
    inScope(WEEKLY(nextWeek),nowExact) must beFalse
    inScope(WEEKLY(nextWeek),nowInScope) must beTrue
    inScope(MONTHLY(nextMonth),nowExact) must beFalse
    inScope(MONTHLY(nextMonth),nowInScope) must beTrue
    inScope(YEARLY(nextYear),nowExact) must beFalse
    inScope(YEARLY(nextYear),nowInScope) must beTrue
  }

   "be able to create a postponed start" in {
      " A Daily schedule that starts more than 1 day later should be a yearly schedule to start the daily schedule" in {
        todo
      }
   }



  }
}
