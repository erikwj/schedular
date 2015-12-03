package models

import play.api.libs._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import java.text.SimpleDateFormat


/*
val msg = EmailService.mailmessage(
"Simple email", 
Seq("Miss TO <to@email.com>", "Mr TO <to@email.com>"), 
Some("A text message"), 
"dhl.pdf",
"/Users/erikjanssen/dhl.pdf")

 curl -H "Content-Type: application/json" -X POST \
 --data '{"subject":"test report","body":"This is your requested report","to":["Miss TO <to@email.com>"],"reportName":"DHL report","url": "?period=2015-11-30T00:00%2F2015-12-06T23:59","schedule":"*5 * * ? * *" }' http://localhost:9000/sendOnce
 */      

case class ScheduleReportToBeSent(subject: String, body: String, to: Seq[String],reportName:String,  url: String, schedule: String)
case class ReportSenderNow(subject: String, body: String, to: Seq[String],reportName:String, url: String)
case class JobId(id: String)

object Formatters {
	implicit val fmtSRTBS = Json.format[ScheduleReportToBeSent]
	implicit val fmtSRN = Json.format[ReportSenderNow]
	implicit val fmtJI = Json.format[JobId]
}

object JobId {
	val validId = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$".r
	def isValid(id:String):Boolean = validId.findFirstIn(id) match {
		case Some(id) => true
		case _ => false 
	}
}

object Schedule {
  val dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
}
