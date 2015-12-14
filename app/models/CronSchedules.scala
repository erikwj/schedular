package models
import play.api.libs._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import java.util.Date

sealed trait CronSchedule {
  val startDate: Date
}

case class DAILY(startDate: Date) extends CronSchedule
case class WEEKLY(startDate: Date) extends CronSchedule
case class MONTHLY(startDate: Date) extends CronSchedule
case class YEARLY(startDate: Date) extends CronSchedule
case class ONCE(startDate: Date) extends CronSchedule

object CronSchedule {

  val DAILY_ID = "daily"
  val WEEKLY_ID = "weekly"
  val MONTHLY_ID = "monthly"
  val YEARLY_ID = "yearly"
  val ONCE_ID = "once"

  val ds = Json.format[DAILY]
  val ws = Json.format[WEEKLY]
  val ms = Json.format[MONTHLY]
  val ys = Json.format[YEARLY]
  val o = Json.format[ONCE]

  implicit val fmt: Format[CronSchedule] = new Format[CronSchedule] {
    def reads(json: JsValue): JsResult[CronSchedule] = json \ "schedule" match {
      case JsString(DAILY_ID)      ⇒ Json.fromJson[DAILY](json)(ds)
      case JsString(WEEKLY_ID)     ⇒ Json.fromJson[WEEKLY](json)(ws)
      case JsString(MONTHLY_ID)    ⇒ Json.fromJson[MONTHLY](json)(ms)
      case JsString(YEARLY_ID)    ⇒ Json.fromJson[YEARLY](json)(ys)
      case JsString(ONCE_ID)    ⇒ Json.fromJson[ONCE](json)(o)
      case _                       ⇒ JsError(s"Unexpected JSON value $json")
    }

    def writes(cs: CronSchedule): JsValue = cs match {
      case schedule: DAILY     ⇒ (Json.toJson(schedule)(ds)).as[JsObject] + ("schedule" -> Json.toJson(DAILY_ID))
      case schedule: WEEKLY    ⇒ (Json.toJson(schedule)(ws)).as[JsObject] + ("schedule" -> Json.toJson(WEEKLY_ID))
      case schedule: MONTHLY   ⇒ (Json.toJson(schedule)(ms)).as[JsObject] + ("schedule" -> Json.toJson(MONTHLY_ID))
      case schedule: YEARLY    ⇒ (Json.toJson(schedule)(ys)).as[JsObject] + ("schedule" -> Json.toJson(YEARLY_ID))
      case schedule: ONCE    ⇒ (Json.toJson(schedule)(o)).as[JsObject] + ("schedule" -> Json.toJson(ONCE_ID))
    }
  }

  val update = (s:CronSchedule,d:Date) => s match {
    case DAILY(_) => Some(DAILY(d))
    case WEEKLY(_) => Some(WEEKLY(d))
    case MONTHLY(_) => Some(MONTHLY(d))
    case YEARLY(_) => Some(YEARLY(d))
    case ONCE(_) => Some(ONCE(d))
    case _ => None
  }


  

}