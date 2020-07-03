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
import quizleague.web.service.results.ReportGetService
import quizleague.web.service.PostService
import quizleague.domain.command.ResultsSubmitCommand
import quizleague.domain.command.ResultValues
import quizleague.domain.Key
import java.time.LocalDate.{now => today}
import java.time.LocalTime

import rxscalajs.Observable
import java.time.LocalDateTime

import quizleague.web.service.competition.CompetitionGetService
import quizleague.web.site.ApplicationContextService
import quizleague.web.site.chat.ChatService

object FixturesModule extends Module {

  override val components = @@(SimpleFixturesComponent, AllFixturesComponent)
}

object FixturesService extends FixturesGetService {
  override val fixtureService = FixtureService
  override val competitionService = CompetitionService

  def nextFixtures(seasonId: String): Observable[js.Array[Fixtures]] = {
    val today = LocalDate.now.toString
    val now = LocalDateTime.now.toString
    val fixtures = seasonFixtures(seasonId)

    fixtures.map(_.filter(f => now <= s"${f.date}T${f.start}").toSeq.sortBy(_.date).headOption.fold(js.Array[Fixtures]())(f => js.Array(f)))
  }
  def latestResults(seasonId:String): Observable[js.Array[Fixtures]] = {
    val today = LocalDate.now.toString

    val now = LocalDateTime.now.toString

    val fixtures = seasonFixtures(seasonId)

    fixtures.map(_.filter(f => now >= s"${f.date}T${f.start}").toSeq.sortBy(_.date)(Desc).headOption.fold(js.Array[Fixtures]())(f => js.Array(f)))

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
      val interim = competitions.map(_.map(c => FixturesService.list(c.key)))

    interim.flatMap(o => combineLatest(o).map(_.flatten.toJSArray))
  }



}

object FixtureService extends FixtureGetService with PostService{
  override val venueService = VenueService
  override val teamService = TeamService
  override val userService = UserService
  override val fixturesService = FixturesService
  override val reportService  = ReportService

//  def recentTeamResults(teamId: String, take:Int = Integer.MAX_VALUE): Observable[js.Array[Fixture]] = {
//    val q = groupQuery().where("date","<=", today.toString).orderBy("date","desc").limit(take)
//    val home = query(q.where("home.id","==",teamId))
//    val away = query(q.where("away.id","==",teamId))
//
//    Observable.combineLatest(Seq(home,away)).map(_.flatMap(x=>x).sortBy(_.date)(Desc).take(take).toJSArray)
//  }

  def fixturesFrom(fixtures:Observable[js.Array[Fixtures]], teamId:String, take:Int = Integer.MAX_VALUE, sortOrder:Ordering[String] = Asc[String]) = {
    val tf = fixturesToFixtureList(fixtures.map(_.sortBy(_.date)(sortOrder)))
      .map(_.filter(f => f.home.id == teamId || f.away.id == teamId))

    tf.map(_.take(take))
  }
  
  def teamResults(teamId: String, seasonId: String, take:Int = Integer.MAX_VALUE): Observable[js.Array[Fixture]] = {
    
    val fixtures = FixturesService.spentFixtures(seasonId).map(_.sortBy(_.date)(Desc))
    
    val tf = fixturesToFixtureList(fixtures)
    .map(_.filter(f => (f.home.id == teamId || f.away.id == teamId)))
      
    tf.map(_.take(take))
  }
    
  def teamFixturesForSeason(teamId: String, seasonId: String, take:Int = Integer.MAX_VALUE): Observable[js.Array[Fixture]] = {
    
    val fixtures = FixturesService.activeFixtures(seasonId)

    fixturesFrom(fixtures, teamId, take, Asc)
  }


  def fixturesForResultSubmission(teamId:String) = {
    val today = LocalDate.now.toString()
    val now = today + LocalTime.now().toString()
    val context = ApplicationContextService.get()

    val comps = context
      .flatMap(_.currentSeason)
      .flatMap(_.competition)

    val triples = comps
      .map(
        _.map(c =>
          c.fixtures
            .map(_.filter(f => f.date <= today && f.start <= now)
              .map(f => (c,f))
                .map{case(c,f) => f.fixture
                  .map(fx => (c,f,fx.filter(x => x.home.id == teamId || x.away.id == teamId)))}

        )

      ))
      .flatMap(x =>combineLatest(x.toSeq))
        .map(_.toJSArray.flatten.toSeq)
        .flatMap(x => combineLatest(x))
        .map(_.toJSArray)



    val fixtures =
      triples.map(
        _.groupBy{case(c,f,fxs) => f.date}
          .toList
          .sortBy(_._1)(Desc)
          .take(1)
          .map{case(c,v) => v}
          .toJSArray.flatten.toJSArray)
          .map(_.sortBy{case(c,f,fxs) => c.subsidiary})
          .map(_.flatMap{case(c,f,fxs) => fxs})

   fixtures

  }
  

  def submitResult(fixtures:js.Array[Fixture], reportText:String, userID:String) = {
    import quizleague.util.json.codecs.CommandCodecs._
    
    val cmd = ResultsSubmitCommand(fixtures.map(f => ResultValues(Key(f.key.key), f.result.homeScore, f.result.awayScore)).toList, Option(reportText), userID)
    
    command[List[String],ResultsSubmitCommand](List("site","result","submit"),Some(cmd)).subscribe(x => Unit)
  }
  
}

object ReportService extends ReportGetService {
  val textService = TextService
  val teamService = TeamService
}

