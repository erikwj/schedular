import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import java.util.Date
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

import java.text.SimpleDateFormat

import services.CronService
import models._
import models.CronSchedule._


/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class CronScheduleSpec extends Specification {

  val date = new SimpleDateFormat("yyyy-MM-dd HH:mm.ss").parse("2015-12-07 23:44.00")
  val weekly = WEEKLY(date)

  "CronSchedule" should {

    "have a toJson method" in {
          Json.toJson(weekly) must_== Json.parse("""{"schedule":"weekly", "startDate":1449528240000}""") //List(d1,d2,d3,d4,d5,s2,d6)
      
    }

  }
}
