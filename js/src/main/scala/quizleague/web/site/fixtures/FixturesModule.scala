package quizleague.web.site.fixtures

import quizleague.web.service.fixtures.FixtureGetService
import quizleague.web.service.fixtures.FixturesGetService
import quizleague.web.site.team.TeamService
import quizleague.web.site.venue.VenueService
import quizleague.web.model._
import quizleague.web.site.team.TeamService
import quizleague.web.core._
import scalajs.js
import js.JSConverters._
import rxscalajs.Observable
import rxscalajs.Observable._
import quizleague.web.model.CompetitionType
import java.time.LocalDate
import quizleague.web.util.Logging._
import quizleague.web.site.season.SeasonService
import quizleague.web.site.competition.CompetitionService
import quizleague.web.site.user.UserService
import quizleague.util.collection._
import quizleague.web.site.text.TextService
import quizleague.web.service.results.ReportsGetService
import quizleague.web.service.PostService
import quizleague.domain.command.ResultsSubmitCommand
import quizleague.domain.command.ResultValues
import LocalDate.{now => today}
import java.time.LocalTime
import rxscalajs.Observable
import java.time.LocalDateTime

object FixturesModule extends Module {

  override val components = @@(SimpleFixturesComponent, AllFixturesComponent)
}

object FixturesService extends FixturesGetService {
  override val fixtureService = FixtureService

  def nextFixtures(seasonId: String): Observable[js.Array[Fixtures]] = {
    val today = LocalDate.now.toString
    val now = LocalDateTime.now.toString
    
    val q = db.collection(uriRoot).where("date", ">=" , today).orderBy("date").limit(10)
      
    query(q).map(_.filter(!_.subsidiary).filter(f => now <= s"${f.date}T${f.start}").groupBy(_.date).toSeq.sortBy(_._1).headOption.fold(js.Array[Fixtures]())(_._2))
    
  }
  def latestResults(seasonId:String): Observable[js.Array[Fixtures]] = {
    val today = LocalDate.now.toString
    
    val now = LocalDateTime.now.toString
    
    val q = db.collection(uriRoot).where("date", "<=" , today).orderBy("date","desc").limit(10)
    
    query(q).map(_.filter(!_.subsidiary).filter(f => now > s"${f.date}T${f.start}").groupBy(_.date).toSeq.sortBy(_._1)(Desc).headOption.fold(js.Array[Fixtures]())(_._2))
  }
  
  def activeFixtures(seasonId: String, take:Int = Integer.MAX_VALUE) = {
    val today = LocalDate.now.toString()

    seasonFixtures(seasonId).map(_.filter(_.date >= today).sortBy(_.date).take(take))
  }
  
  def spentFixtures(seasonId: String, take:Int = Integer.MAX_VALUE) = {
    val today = LocalDate.now.toString()

    seasonFixtures(seasonId).map(_.filter(_.date <= today).sortBy(_.date)(Desc).take(take))
  }

  private def seasonFixtures(seasonId:String) = {
    competitionFixtures(CompetitionService.firstClassCompetitions(seasonId))
  }
  
  def competitionFixtures(competitions:Observable[js.Array[_ <: Competition]]):Observable[js.Array[Fixtures]] = {
      competitions.map(_.flatMap(_.fixtures.map(_.obs))).flatMap(o => combineLatest(o).map(_.toJSArray))
  }

}

object FixtureService extends FixtureGetService with PostService{
  override val venueService = VenueService
  override val teamService = TeamService
  override val userService = UserService
  override val reportsService = ReportsService

    
  def teamFixtures(teamId: String, take:Int = Integer.MAX_VALUE): Observable[js.Array[Fixture]] = {
    val q = db.collection(uriRoot).where("date",">=", today.toString).where("subsidiary","==", false).orderBy("date").limit(take)
    val home = query(q.where("home.id","==",teamId))
    val away = query(q.where("away.id","==",teamId))
    
    Observable.combineLatest(Seq(home,away)).map(_.flatMap(x=>x).sortBy(_.date).take(take).toJSArray)
  }
  
  def recentTeamResults(teamId: String, take:Int = Integer.MAX_VALUE): Observable[js.Array[Fixture]] = {
    val q = db.collection(uriRoot).where("date","<=", today.toString).where("subsidiary","==", false).orderBy("date","desc").limit(take)
    val home = query(q.where("home.id","==",teamId))
    val away = query(q.where("away.id","==",teamId))
    
    Observable.combineLatest(Seq(home,away)).map(_.flatMap(x=>x).sortBy(_.date)(Desc).take(take).toJSArray)
  }
  
  def fixturesFrom(fixtures:Observable[js.Array[Fixtures]], teamId:String, take:Int = Integer.MAX_VALUE, sortOrder:Ordering[String] = Asc[String]) = {
    val tf = fixtures.flatMap(fx => combineLatest(fx.flatMap(_.fixtures).map(_.obs)))
    .map(_.filter(f => f.home.id == teamId || f.away.id == teamId).sortBy(_.date)(sortOrder))
      
    tf.map(_.take(take).toJSArray)
  }
  
  def teamResults(teamId: String, seasonId: String, take:Int = Integer.MAX_VALUE): Observable[js.Array[Fixture]] = {
    
    val fixtures = FixturesService.spentFixtures(seasonId)
    
    val tf = fixtures.switchMap(fx => combineLatest(fx.flatMap(_.fixtures).map(_.obs)))
    .map(_.filter(f => (f.home.id == teamId || f.away.id == teamId)).sortBy(_.date)(Desc))
      
    tf.map(_.take(take).toJSArray)
  }
    
  def teamFixturesForSeason(teamId: String, seasonId: String, take:Int = Integer.MAX_VALUE): Observable[js.Array[Fixture]] = {
    
    val fixtures = FixturesService.activeFixtures(seasonId)
    
    val tf = fixtures.switchMap(fx => combineLatest(fx.flatMap(_.fixtures).map(_.obs)))
    .map(_.filter(f => f.home.id == teamId || f.away.id == teamId).sortBy(_.date))
      
    tf.map(_.take(take).toJSArray)
  }

  def fixturesForResultSubmission(email: String, seasonId: String): Observable[js.Array[Fixture]] = {

    val today = LocalDate.now.toString()
    val now = today + LocalTime.now().toString()

    val fixtures: Observable[js.Array[Fixture]] = teamService.teamForEmail(email)
      .map(
        _.map(
          team => recentTeamResults(team.id,4).map(
                  _.groupBy(_.date)
                  .toList
                  .sortBy(_._1)(Desc)
                  .take(1)
                  .map { case (k, v) => v }
                  .toJSArray
                  .flatMap(x => x))))
      .map(x => combineLatest(x.toSeq))
      .flatten
      .map(_.toJSArray.flatMap(x => x))

    fixtures.map(_.filter(f => (f.date + f.time) <= now))

  }
  

  def submitResult(fixtures:js.Array[Fixture], reportText:String, email:String) = {
    import quizleague.util.json.codecs.CommandCodecs._
    
    val cmd = ResultsSubmitCommand(fixtures.map(f => ResultValues(f.id, f.result.homeScore, f.result.awayScore)).toList, Option(reportText), email)
    
    command[String,ResultsSubmitCommand](List("site","result","submit"),Some(cmd)).subscribe(x => Unit)
  }
  
}

object ReportsService extends ReportsGetService {
  val textService = TextService
  val teamService = TeamService
}

