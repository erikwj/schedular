package repositories

import models._
import java.util.Date

object RedisBackupRepository extends BackupRepository with RedisRepository {
  private val JOBSKEY = "jobids"

  def jobIds:List[String] = redis.lrange(JOBSKEY,0,-1).getOrElse(List[Option[String]]()).flatten

  def get(id:String):Option[String] = redis.get(id)

  def getField(id:String,field:String):Option[String] = redis.hget(id,field)
   
  def remove(id: JobId):Unit = {
    //remove the key-value pair
    redis.del(id)
    //remove the id from the list of active jobids
    redis.lrem(JOBSKEY,0,id)
  }

  def addScheduledReport(id:JobId, report: ScheduledReport) = {
    redis.hmset(id,Map("reportName" -> report.reportName, "url" -> report.url, "schedule" -> report.schedule))
  }

  def addNextRun(id:JobId, nextRun: Long):Boolean = {
    val result = redis.hmset(id,Map("nextRun" -> nextRun))
    // println("addNextRun " + id)
    // println(result)
    result
  }

  def nextRuns(ids:List[JobId]): List[Date] = (ids map { id => {
    println("nextRuns " + id)
    val r = redis.hmget(id,"nextRun").getOrElse(Map[String,String]())
    // println(r)
    r.get("nextRun") map { s => new Date()}
  }}).flatten

  def scheduled = jobIds map { id => (id,redis.hmget[String,String](id,"reportName", "nextRun").getOrElse(Map[String,String]()))}

  def add(id:String,value:play.api.libs.json.JsValue):Option[Int] = {
    //add the key value pair
    val success = redis.set(id, value)
    //add the id to the list of active jobids
    if(success) {
      redis.lpush(JOBSKEY,id) map {_.toInt}
    }
    else None
  }
}