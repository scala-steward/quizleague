package quizleague.rest.endpoint

import javax.ws.rs.Path
import quizleague.rest.MaintainPostEndpoints
import javax.ws.rs.POST
import quizleague.domain.container.{DomainContainer,NestedDomainContainer}
import scala.reflect.ClassTag
import quizleague.data.Storage
import Storage._
import quizleague.domain._
import javax.ws.rs.PathParam
import quizleague.domain._
import quizleague.domain.stats._
import quizleague.domain.util._
import quizleague.util.json.codecs.DomainCodecs._
import quizleague.conversions.RefConversions._
import quizleague.rest._ 
import io.circe._
import javax.ws.rs.GET
import javax.ws.rs.Produces
import com.google.appengine.api.taskqueue._
import com.google.appengine.api.taskqueue.TaskOptions.Builder._
import java.util.logging.Logger

@Path("/entity")
class EntityEndpoint extends MaintainPostEndpoints{
  
  val LOG:Logger = Logger.getLogger(this.getClass.getName)
  
  implicit val context = StorageContext()
  
  @POST
  @Path("/dbupload")
  def dbupload(json:String) = {
    
    def saveAll[T <: Entity](list:List[T])(implicit tag:ClassTag[T], encoder:Encoder[T]) = {
      Storage.saveAll[T](list)(tag,encoder)
    }
    
    def deleteAll[T <: Entity](implicit tag:ClassTag[T], decoder:Decoder[T]){
      try{
        Storage.deleteAll(Storage.list[T])
      }
      catch{case e:Throwable => LOG.info(s"exception deleting collection $tag")}
    }
    
    deleteAll[ApplicationContext]
    deleteAll[Competition]
    deleteAll[Fixture]
    deleteAll[Fixtures]
    deleteAll[GlobalText]
    deleteAll[LeagueTable]
    deleteAll[Reports]
    deleteAll[Season]
    deleteAll[Team]
    deleteAll[Text]
    deleteAll[User]
    deleteAll[Venue]
    deleteAll[Statistics]
    
    val container = deser[DomainContainer](json)
    
    saveAll(container.applicationcontext)
    saveAll(container.competition)
    saveAll(container.fixture)
    saveAll(container.fixtures)
    saveAll(container.globaltext)
    saveAll(container.leaguetable)
    saveAll(container.reports)
    saveAll(container.season)
    saveAll(container.team)
    saveAll(container.text)
    saveAll(container.user)
    saveAll(container.venue)
    saveAll(container.competitionStatistics)

    
    
    
  }

  @POST
  @Path("/nesteddbupload")
  def nesteddbupload(json:String) = {


    def withKey[T <: Entity](pair:(String,T)):T =  {val (key,entity) = pair; entity.withKey(Key(key))}

    def saveAll[T <: Entity](list:Map[String, T])(implicit tag:ClassTag[T], encoder:Encoder[T]) = {
      Storage.saveAll[T](list.toList.map(withKey _))(tag,encoder)
    }

    def deleteAll[T <: Entity](implicit tag:ClassTag[T], decoder:Decoder[T]){
      try{
        Storage.deleteAll(Storage.list[T])
      }
      catch{case e:Throwable => LOG.info(s"exception deleting collection $tag")}
    }

    deleteAll[ApplicationContext]
    deleteAll[Competition]
    deleteAll[Fixture]
    deleteAll[Fixtures]
    deleteAll[GlobalText]
    deleteAll[LeagueTable]
    deleteAll[Reports]
    deleteAll[Season]
    deleteAll[Team]
    deleteAll[Text]
    deleteAll[User]
    deleteAll[Venue]
    deleteAll[Statistics]
    deleteAll[CompetitionStatistics]

    val container = deser[NestedDomainContainer](json)


    saveAll(container.applicationcontext)
    saveAll(container.competition)
    saveAll(container.fixture)
    saveAll(container.fixtures)
    saveAll(container.globaltext)
    saveAll(container.leaguetable)
    saveAll(container.reports)
    saveAll(container.season)
    saveAll(container.team)
    saveAll(container.text)
    saveAll(container.user)
    saveAll(container.venue)
    saveAll(container.competitionStatistics)
    saveAll(container.chat)
    saveAll(container.chatMessage)
  }
  
  @GET
  @Path("/dbdownload/dump.json")
  @Produces(Array("application/json"))
  def dbdownload() = {
    import Storage.list
    import io.circe.syntax._

    
    val container = DomainContainer(
        list[ApplicationContext],
        list[Competition],
        list[Fixtures],
        list[Fixture],
        list[GlobalText],
        list[LeagueTable],
        list[Reports],
        list[Season],
        list[Team],
        list[Text],
        list[User],
        list[Venue],
        list[CompetitionStatistics]
    )
    
    container.asJson.noSpaces
  }
  
  @POST
  @Path("/recalculate-table/")
  def recalculateTable(json:String) = {
        val key =  deser[Key](json)

        val table = load[LeagueTable](key)
        val fixtures = list[Fixtures](key.parentKey.map(Key(_))).flatMap(fxs => list[Fixture](fxs.key))

        LOG.info(s"fixtures : $fixtures")
        
        val blankTable = table.copy(rows = table.rows.map(_.copy(won=0,lost=0,drawn=0,leaguePoints=0,matchPointsFor=0, matchPointsAgainst=0, played=0))).withKey(key)
        
        val recalcTable = LeagueTableRecalculator.recalculate(List(blankTable), fixtures)
        
        recalcTable.foreach(Storage.save(_))
  }
  
  @POST
  @Path("/regenerate-stats/{seasonId}")
  def regenerateStats(@PathParam("seasonId") seasonId: String) {

   val queue: Queue = QueueFactory.getQueue("stats");
    
   queue.add(withUrl(s"/rest/task/stats/regenerate/$seasonId"));
    
//    val season = load[Season](seasonId)
//    
//    HistoricalStatsAggregator.perform(season)
   

  }
}