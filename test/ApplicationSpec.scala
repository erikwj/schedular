import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
// import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import models._
import actors._
import services._
import repositories._
import ReportSender._
import Formatters._
import quartzscheduler._
import quartzscheduler.QuartzScheduler._
import quartzscheduler.CronSchedule._


/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  val fakeApplication = FakeApplication()

  "Application" should {

    "send 404 on a bad request" in new WithApplication(fakeApplication){
      val Some(result) = route(FakeRequest(GET, "/Bob"))
      status(result) must equalTo(404)
      contentType(result) must beSome("text/plain")
      charset(result) must beSome("utf-8")
    }


    "render the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/plain")
      contentAsString(home) must contain ("running")
    }

/*     curl -H "Content-Type: application/json" -X POST \
 --data '
val data = Json.parse("""{"subject":"test report","body":"This is your requested report","to":["Miss TO <to@email.com>"],"reportName":"DHL report","url": "?period=2015-11-30T00:00%2F2015-12-06T23:59","scheme":{"schedule":"weekly", "startDate":1449528240000} }""")
 ' http://localhost:9000/scheduleReport
*/ 

"post a new schedule,  using route POST /scheduleReport" in {
  running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {

    val data = Json.parse("""{"subject":"test report","body":"This is your requested report","to":["Miss TO <to@email.com>"],"reportName":"DHL report","url": "?period=2015-11-30T00:00%2F2015-12-06T23:59","scheme":{"schedule":"weekly", "startDate":1449528240000} }""")

    val Some(result) = route(
      FakeRequest(
        POST, 
        "/scheduleReport",
        FakeHeaders(Seq("Content-Type" -> "application/json")), 
        data
      )
    )
    // result map { r => println(r.body)}
    println(contentAsJson(result))
    status(result) must equalTo(OK)
      
  // todo
  }
}




  }
}
