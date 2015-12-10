import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import java.util.Date
import play.api.test._
import play.api.test.Helpers._

import java.text.SimpleDateFormat

import services.CronService
import services.CronService._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class CronServiceSpec extends Specification {

  val s = new SimpleDateFormat("yyyy-MM-dd HH:mm.ss").parse("2015-12-07 23:44.00")
  val s2 = new SimpleDateFormat("yyyy-MM-dd HH:mm.ss").parse("2015-12-07 23:44.05")
  val s3 = new SimpleDateFormat("yyyy-MM-dd HH:mm.ss").parse("2015-12-07 23:44.039")
  val d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm.ss").parse("2015-12-07 23:44.00")
  val d2 = new SimpleDateFormat("yyyy-MM-dd HH:mm.ss").parse("2015-12-07 23:44.01")
  val d3 = new SimpleDateFormat("yyyy-MM-dd HH:mm.ss").parse("2015-12-07 23:44.02")
  val d4 = new SimpleDateFormat("yyyy-MM-dd HH:mm.ss").parse("2015-12-07 23:44.03")
  val d5 = new SimpleDateFormat("yyyy-MM-dd HH:mm.ss").parse("2015-12-07 23:44.04")
  val d6 = new SimpleDateFormat("yyyy-MM-dd HH:mm.ss").parse("2015-12-07 23:44.15")



  "CronService" should {

    "have a method that returns the next available timeslot where" in {
          val dates = List(d1,d2,d3,d4,d5,d6)
          val newDate = CronService.getStartDate(s,dates,1000) 
          val newDate3 = CronService.getStartDate(s3,dates,1000) 
          val newDate2 = CronService.getStartDate(s,List[Date](),2000) 
          newDate must_== s2 //List(d1,d2,d3,d4,d5,s2,d6)
      "a new unique date is returned" in {
          newDate3 must_== s2 //List(d1,d2,d3,d4,d5,s2,d6)
          newDate2 must_== s
      }

    }
   "be able to generate cronschedule from startdate" in {
    CronService.toCron(s2,DAILY) must_== "5 44 23 * * * *"
    CronService.toCron(s2,WEEKLY) must_== "5 44 23 * * 1 *"
    CronService.toCron(s2,MONTHLY) must_== "5 44 23 11 * * *"
   }

  }
}
