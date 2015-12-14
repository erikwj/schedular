package test

import org.scalacheck._
import org.scalacheck.Properties
import org.scalacheck.Prop.forAll
import org.scalacheck.Arbitrary.arbitrary

import java.util.Date
import services.CronService

object CronServiceSpecification extends Properties("Cron") {

  def timeBetween(dates:List[Date]): List[Long] = {
    val sorted = dates.sortBy(_.getTime())
    val windows = (sorted map {_.getTime}).sliding(2).toList
    windows map ( pair => pair(1) - pair(0))
  }

  val genDate 		   = arbitrary[Date]
  val genDateList      = Gen.containerOfN[List,Date](100,arbitrary[Date])

  property("getStartDate") = forAll(arbitrary[Date],genDateList) { (a: Date, b: List[Date]) =>
    val m = 1000
    val date = CronService.getStartDate(a,b,m) 
    !b.contains(date) 
  }

}