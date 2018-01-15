package quizleague.rest.endpoint


import javax.ws.rs.core._
import javax.ws.rs._
import quizleague.data.Storage
import quizleague.util.json.codecs.CommandCodecs._
import quizleague.util.json.codecs.DomainCodecs._
import quizleague.conversions.RefConversions._
import quizleague.domain._
import quizleague.domain.command._
import quizleague.domain.util._
import java.util.UUID.{randomUUID => uuid}
import java.util.logging.Logger



@Path("/task")
class TaskEndpoint {
  
  val logger = Logger.getLogger(this.getClass.getName)
  implicit val context = StorageContext
  
  @POST
  @Path("/submitresult")
  def resultSubmit(body:String) = {
   import io.circe._, io.circe.generic.auto._, io.circe.parser._
   
   val result = decode[ResultsSubmitCommand](body).merge.asInstanceOf[ResultsSubmitCommand]
    
   logger.finest(s"submit result arrived : $result") 
   
   val haveResults = result.fixtures.exists(f => {
     val fix = Storage.load[Fixture](f.fixtureId)
     fix.result.isDefined
   })
   
   userFromEmail(result.email).foreach(u => result.fixtures.foreach(saveFixture(u,result.reportText) _))
   
   logger.finest(s"haveResults : $haveResults") 
   
   if(!haveResults) updateTables(result)
   
  }
  
  private def updateTables(result:ResultsSubmitCommand){
    
    def compTables(comp:Ref[Competition]):List[LeagueTable] = {
      refToObject(comp) match {
        case c:CompetitionTables => c.tables
        case _ => List()
      }
    }
    
    logger.finest(s"entering updateTables : \nresult:$result") 
    
    val tables = Storage.load[ApplicationContext](ApplicationContext.singletonId)
    .currentSeason
    .competitions
    .flatMap(compTables _)
    
    logger.finest(s"tables : \n$tables") 
    
    val fixtures = result.fixtures.map(f => Storage.load[Fixture](f.fixtureId))
    
    logger.finest(s"fixtures : \n$fixtures") 
    
    val newTables = LeagueTableRecalculator.recalculate(tables, fixtures)
    
    logger.finest(s"new tables : \n$newTables") 
    
    newTables.foreach(Storage.save(_))
    
  }

  private def saveFixture(user:User,report:Option[String])(result:ResultValues) = {
       
    logger.finest(s"entering saveFixture : \nuser : $user\nreport : $report\nresult:$result") 
    
    def newText(reportText:String) = {
      val text = new Text(uuid.toString(), reportText, "text/plain")
      Storage.save(text)
      Ref[Text]("text",text.id)
    }
    
    def newReports(reportText:String) = {

      val reports = Reports(uuid.toString(), List(newReport(reportText)))
      Storage.save(reports)
      Ref[Reports]("reports",reports.id)
    }
    
    def newResult() = {
      Some(Result(result.homeScore,result.awayScore, submitter = Some(Ref[User]("user",user.id)), None, report.map(newReports(_))))
    }
    
    def newReport(reportText:String) = {
       val team = teamFromUser(user)
       Report(Ref("team",team.id), newText(reportText))
    }
    
    def addReport(ref:Reports, reportText:String) = {
      val newRef = ref.copy(reports = ref.reports.:+(newReport(reportText)))
      Storage.save(newRef)
      Ref[Reports]("reports",ref.id)
    }
    
    def oldResult(existing:Result) = {
      val reports = report.fold(existing.reports)(t => Some(existing.reports.fold(newReports(t))(addReport(_, t))))
      Some(existing.copy(reports = reports))

    }
    
    
    val fixture = Storage.load[Fixture](result.fixtureId)
    
    logger.finest(s"fixture : \n:$fixture") 
    
    val res = fixture.copy(result = fixture.result.fold(newResult())(oldResult _))
    
    logger.finest(s"made result : \nresult:$res") 
    
    Storage.save(res)

  }

  private def userFromEmail(email:String):Option[User] = {

    val users = Storage.list[User]
    
    logger.finest(s"users : \n$users") 
    
    val user = users.filter(_.email.toLowerCase == email.toLowerCase).headOption
    
    logger.finest(s"user : \n$user")
    
    user
  }
  
  private def teamFromUser(user:User) = {
    
    val teams = Storage.list[Team]
    
    logger.finest(s"teams : \n$teams") 
    
    val team = teams.filter(t => t.users.exists(_.id == user.id)).head
    
    logger.finest(s"team : \n$team")
    
    team
  }
  
}