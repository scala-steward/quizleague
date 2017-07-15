package quizleague.web.maintain.leaguetable

import angulate2.std._
import angular.material.MaterialModule
import angulate2.forms.FormsModule
import angulate2.router.{Route,RouterModule}

import scala.scalajs.js

import angulate2.http.Http
import angular.flexlayout.FlexLayoutModule
import quizleague.web.names.ComponentNames


import angulate2.common.CommonModule
import quizleague.web.service.leaguetable._

import angulate2.ext.classModeScala
import quizleague.web.maintain._
import quizleague.web.maintain.fixtures.FixturesService
import quizleague.web.maintain.fixtures.FixtureService
import quizleague.web.maintain.team.TeamService
import quizleague.web.maintain.text.TextService
import quizleague.web.maintain.user.UserService
import quizleague.web.maintain.results.ResultsService
import quizleague.web.service.PostService
import quizleague.web.model._
import quizleague.domain.{LeagueTable => Dom}
import quizleague.util.json.codecs.DomainCodecs._
import quizleague.web.util.Logging._

@NgModule(
  imports = @@[CommonModule,FormsModule,MaterialModule,RouterModule,FlexLayoutModule],
  declarations = @@[LeagueTableComponent,LeagueTableListComponent],
  providers = @@[LeagueTableService]
   
)
class LeagueTableModule


@Injectable
@classModeScala
class LeagueTableService(
    override val http: Http,
    override val teamService:TeamService,
    override val resultsService:ResultsService
) extends LeagueTableGetService with LeagueTablePutService with PostService[LeagueTable] with ServiceRoot{
  
  def recalculateTable(table:LeagueTable, competition:Competition) = {
    val res = command[Dom,String](List(table.id,"competition",competition.id, "recalc"),None)
    res.map((u,i) => mapOutSparse(u))
  }
  
}


