import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
// import play.api.mvc._
import scala.concurrent.Future
/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/boum")) must beNone
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

    val result:Option[Future[Result]] = route(
      FakeRequest(
        POST, 
        "/scheduleReport",
        FakeHeaders(Seq("Content-Type" -> Seq("application/json"))), 
        data
      )
    )

    // status(result) must equalTo(OK)
    // contentType(result) must beSome("application/json")
    // val Some(ideaType) = parse(contentAsString(result)).asOpt[IdeaType]

    // ideaType.name mustEqual "new name"

  }
}


  }
}
