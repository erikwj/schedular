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


/*
Schedule once is just resend the request later

*/
  // object JobCreator {
  //   case class StartJob(wsRequestHolder: WsRequestHolder, json: JsValue) 
  // }

  // class JobCreator(bookmarkUrl:String) extends Actor with ActorLogging {
  //   //apitools elogistics
  //   // implicit val configuration = Play.current.configuration
  //   // lazy implicit val alertFunction = Requests.eMailRequestFailure(_, _, _)
  //   // lazy implicit val actorSystem = play.api.libs.concurrent.Akka.system
  //   // lazy implicit val supervisor: ActorRef = processes.httpSupervisor


  //   import JobCreator._
  //   def receive: Receive = {
  //     case StartJob(wsRequestHolder, json) => {
  //       log.info(s"received create report")
  //       Requests.sendRequest(wsRequestHolder,json)
  //     }
  //   }

  // }



