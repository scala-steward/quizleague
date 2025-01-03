package quizleague.web.maintain.fixtures

import quizleague.domain
import quizleague.web.service.fixtures.FixtureGetService
import quizleague.web.service.fixtures.FixturePutService
import quizleague.web.maintain.team.TeamService
import quizleague.web.maintain.venue.VenueService
import quizleague.web.service.fixtures.FixturesGetService
import quizleague.web.service.fixtures.FixturesPutService
import quizleague.web.service.results.ReportGetService
import quizleague.web.service.results.ReportPutService
import quizleague.web.maintain.text.TextService
import quizleague.web.maintain.user.UserService
import quizleague.web.maintain.competition.CompetitionService
import rxscalajs.Observable._

import scalajs.js
import js.JSConverters._
import quizleague.web.core._
import quizleague.web.core.RouteConfig
import quizleague.web.maintain.MaintainMenuComponent
import quizleague.web.model._
import quizleague.domain.Result
import quizleague.domain.{Key => DomKey}
import quizleague.web.maintain.chat.ChatService
import quizleague.web.service.RetiredFilter
import quizleague.web.service.competition.CompetitionGetService
import quizleague.web.service.team.TeamGetService
import quizleague.web.service.user.UserGetService
import quizleague.web.service.venue.{VenueGetService, VenuePutService}
import quizleague.web.util.Logging
import quizleague.web.util.rx.RefObservable
import rxscalajs.Observable

object FixturesModule extends Module {
  override val routes = @@(     
      RouteConfig(
        path = "/maintain/season/:seasonId/competition/:id/fixtures",
        components = Map("default" -> FixturesListComponent)
      ),
      RouteConfig(
        path = "/maintain/season/:seasonId/competition/:id/fixtures/:fixturesId",
        components = Map("default" -> FixturesComponent)
      ),
      
  )
  
}

object FixtureService extends FixtureGetService with FixturePutService{
  override val teamService = TeamService
  override val venueService = VenueService
  override val userService = UserService
  override val fixturesService = FixturesService
  override val reportService = ReportService
  
  def addResult(fixture:Fixture) = {
    val fx = mapIn(fixture).withKey(DomKey(fixture.key.key))

    add(mapOutWithKey(fx.copy(result = Some(Result(0,0,None,None))).withKey(DomKey(fixture.key.key))))
  }
  
  def copy(fxs:Fixtures):Observable[js.Array[Fixture]] = {
    fxs.fixture.map(_.map(fx => {
      val x = copy(fx)
      x
    }))
  }
  
}

object FixturesService extends FixturesGetService with FixturesPutService{
  override val fixtureService = FixtureService
  override val competitionService = CompetitionService
  
   def copyFixtures(fixtures:Observable[js.Array[Fixtures]], competition:Competition) = {

     def copyFixture(fixture:Fixture, fixtures:Fixtures) = {
       fixtureService.save(fixtureService.copy(fixture, fixtures.key))
     }


     fixtures.map(_.map(fxs => {
       val fixtures1 = copy(fxs, competition.key)
       val saved1 = save(fixtures1).map(Seq(_))
       val saved2 = fxs.fixture.map(_.map(f => copyFixture(f, fixtures1))).map(_.toSeq).map(combineLatest).flatten
       combineLatest(Seq(saved1,saved2))
     }).toSeq).map(combineLatest).flatten.map(_.flatten.flatten)

  }

}

object FilteredVenueService extends VenueGetService with RetiredFilter[Venue]

object FilteredTeamService extends TeamGetService with RetiredFilter[Team]{
  override val userService = UserService
  override val venueService = VenueService
  override val textService = TextService
}

object ReportService extends ReportGetService with ReportPutService{
  override val teamService = TeamService
  override val textService = TextService
}

