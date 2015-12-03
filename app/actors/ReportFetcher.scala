package actors

// import akka.japi.Option.Some
import com.typesafe.config.ConfigFactory
import akka.actor._
import scala.concurrent.duration._
import scala.concurrent.Future
import com.ning.http.client.AsyncHttpClientConfig.Builder
import play.api.libs.ws.ning.NingWSClient
import play.api.libs.json._

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.Logging
import akka.pattern.pipe

import play.api.libs.iteratee._


import java.io.File
import java.io.FileOutputStream
import ReportFetcher._
import actors.ReportSender._

import play.api.libs.mailer._



  object ReportFetcher {

    sealed trait ExcelReport 
    final case class CreateReport(name: String) extends ExcelReport
    final case class ReportSender(name: String,path: String, receipients: Seq[String]) extends ExcelReport
    final case class XLSReport(file:File) extends ExcelReport
    final case class ErrorMessage(name:String, reason: String, receipients: Seq[String]) extends ExcelReport

    def props(url: String): Props = Props(new ReportFetcher(url:String))
  }

  class ReportFetcher(bookmarkUrl:String) extends Actor with ActorLogging {
    def receive: Receive = {
      case CreateReport(name) => {
        log.info(s"received create report")
            
        val futureResponse = client.url("http://localhost:9001/report/excel" + bookmarkUrl).getStream()

        val tempfile = new File(name + ".xlsx")


        val downloadedFile: Future[File] = futureResponse.flatMap {
          case (headers, body) =>
            val outputStream = new FileOutputStream(tempfile)

            // The iteratee that writes to the output stream
            val iteratee = Iteratee.foreach[Array[Byte]] { bytes =>
              outputStream.write(bytes)
            }

            // Feed the body into the iteratee
            (body |>>> iteratee).andThen {
              case result =>
                // Close the output stream whether there was an error or not
                outputStream.close()
                // Get the result or rethrow the error
                result.get
            }.map(_ => tempfile)
        }

          log.info(s"converting downloadedFile")
          downloadedFile pipeTo sender()

      }
    }

    private implicit val dispatcher = context.dispatcher
    private val host = "http://localhost:9001" //ConfigFactory.load().getString("kate.url");
    private val endpoint = host + "/report/excel" //+ bookmark.url  //play config kate uri
    // http://localhost:9000/report/excel?period=2015-11-30T00:00%2F2015-12-06T23:59
    private val client = new NingWSClient(new Builder().build())
  }

