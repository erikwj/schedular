package services


import play.api.libs.concurrent.Akka
import play.api.Play.current
import play.api.libs.mailer._
import org.apache.commons.mail.EmailAttachment
import scala.concurrent.duration._

import akka.actor._
import akka.actor.SupervisorStrategy._
import akka.actor.ActorDSL._
import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging
import akka.routing.SmallestMailboxRoutingLogic
import akka.routing.{ ActorRefRoutee, RoundRobinRoutingLogic, Router }

import java.io.File
import play.api.libs.mailer._

/**
 * Time service for cron jobs
 */
object TimeService {

  final val INTERVAL = 6.minutes
  final val DAILY = 0
  final val WEEKLY = 1
  final val MONTHLY = 3
  /**
   * description
   * @param para
   */
   // def add(date:Date):Date = ???

   // def nextDate(cron: Cron): Cron



}