package quizleague.web.maintain.leaguetable


import quizleague.web.maintain.component.ItemComponent
import quizleague.web.maintain.component._
import quizleague.web.maintain.component.TemplateElements._
import quizleague.web.model._

import scala.scalajs.js
import TemplateElements._
import quizleague.web.maintain.text.TextService

import js.Dynamic.{global => g}
import quizleague.web.util.Logging._
import quizleague.web.maintain.team.TeamService
import quizleague.web.maintain.util.TeamManager
import quizleague.web.util.rx._
import rxscalajs.Observable
import quizleague.web.maintain.competition.CompetitionService
import quizleague.web.util.rx.RefObservable
import quizleague.web.core.RouteComponent

import scalajs.js
import js.JSConverters._
import quizleague.util.collection._
import quizleague.web.maintain.fixtures.FilteredTeamService
import quizleague.web.util.component.{SelectUtils, SelectWrapper}


@js.native
trait LeagueTableComponent extends ItemComponent[LeagueTable]{
  val competition:Competition
  var teamManager:TeamManager
  val teams:js.Array[SelectWrapper[Team]]
}

object LeagueTableComponent extends ItemComponentConfig[LeagueTable] with RouteComponent{
  
  override type facade = LeagueTableComponent
  override val service:LeagueTableService.type = LeagueTableService
  def parentKey(c:facade) =s"season/${c.$route.params("seasonId")}/competition/${c.$route.params("competitionId")}"

  val template = s"""
 <v-container v-if="item && teams">
    <h2>League Tables</h2>
    <v-form v-model="valid">
    <v-layout column>
      <v-layout column>
         <v-text-field label="Description" v-model="item.description" ></v-text-field>
       </v-layout column>
       <v-layout column>
        <h4>Rows</h4>
        <v-layout row>
          <v-btn icon v-on:click="addRow(team)" :disabled="!team" style="position:relative;top:12px"><v-icon>mdi-plus</v-icon></v-btn><v-select label="Team" v-model="team" :items="unusedTeams()"></v-select>
         </v-layout>
         <v-layout column>
           <div><v-btn text v-on:click="recalculate()" color="primary">Recalculate</v-btn></div>
           <div><v-btn text v-on:click="item=sort()" color="primary">Sort</v-btn></div>
          <table>
            <thead>
              <th></th>
              <th>Team</th>
              <th>Position</th>
              <th>Played</th>
              <th>Won</th>
              <th>Lost</th>
              <th>Drawn</th>
              <th>Scored</th>
              <th>Against</th>
              <th>Points</th>
            </thead>
            <tbody>
              <tr v-for="(row,i) in item.rows" :key="row.team.id">
                <td>
                  <v-btn icon v-on:click="removeRow(row)"><v-icon >mdi-delete</v-icon></v-btn>
                </td>
                <td>{{async(row.team).shortName}}</td>
                <td>
                  <v-text-field style="width:3em;" v-model="row.position" length="2"></v-text-field>
                </td>
                <td>
                  <v-text-field style="width:3em;" v-model="row.played" type="number" length="2"></v-text-field>
                </td>
                <td>
                  <v-text-field style="width:3em;" v-model.number="row.won" type="number" length="2"></v-text-field>
                </td>
                <td>
                  <v-text-field style="width:3em;" v-model.number="row.lost" type="number" length="2"></v-text-field>
                </td>
                <td>
                  <v-text-field style="width:3em;" v-model.number="row.drawn" type="number" length="2"></v-text-field>
                </td>
                <td>
                  <v-text-field style="width:5em;" v-model.number="row.matchPointsFor" type="number" length="4"></v-text-field>
                </td>
                <td>
                  <v-text-field style="width:5em;" v-model.number="row.matchPointsAgainst" type="number" length="4"></v-text-field>
                </td>
                <td>
                  <v-text-field style="width:3em;" v-model.number="row.leaguePoints" type="number" length="2"></v-text-field>
                </td>
              </tr>
            </tbody>
          </table>
         </v-layout>
        </v-layout>      
     </v-layout>
     $formButtons
    </v-form>
  </v-container>"""
    
    def removeRow(c:facade, row:LeagueTableRow) = {      
      c.item.rows -= row
      teamManager(c).untake(row.team)
    }
    def addRow(c:facade, team:RefObservable[Team]) = {
      c.item.rows += service.rowInstance(team)
      teamManager(c).take(team)
    }
    
    def recalculate(c:facade) = {
      service.recalculateTable(c.item)
    }
    
    def sort(c:facade) = LeagueTableService.sortTable(c.item)
    
      

  def unusedTeams(c:facade) = teamManager(c).unusedTeams(null)

  def teamManager(c:facade) = {
    if(c.teamManager == null) {
      c.teamManager = new TeamManager(c.teams)
      c.item.rows.foreach(r => c.teamManager.take(r.team))
      c.teamManager
      } 
    else c.teamManager
  }


  def teams() = SelectUtils.model[Team](FilteredTeamService)(_.name)

  method("unusedTeams")({unusedTeams }:js.ThisFunction)
  method("addRow")({addRow }:js.ThisFunction)
  method("removeRow")({removeRow }:js.ThisFunction)
  method("sort")({sort }:js.ThisFunction)
  method("recalculate")({recalculate }:js.ThisFunction)

      
  data("teamManager",null)
  data("team", null) 
  
  subscription("teams")(c => teams())
  subscription("competition")(c => obsFromParam(c,"competitionId",CompetitionService))
  
}
    