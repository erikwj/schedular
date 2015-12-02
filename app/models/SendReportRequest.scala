package models

import play.api.libs._
import play.api.libs.json._
import play.api.libs.functional.syntax._


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
case class SendReportRequest(subject: String, body: String, to: Seq[String],reportName:String,  url: String, schedule: String)

object SendReportRequest {
	implicit val fmt = Json.format[SendReportRequest]
}

