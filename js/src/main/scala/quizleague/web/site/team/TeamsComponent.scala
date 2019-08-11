package quizleague.web.site.team

import quizleague.web.core.RouteComponent
import quizleague.web.core.GridSizeComponentConfig
import quizleague.web.site.SideMenu

object TeamsComponent extends RouteComponent with GridSizeComponentConfig {
    val template="""
      <v-container v-bind="gridSize" fluid>
        <v-layout>
          <v-flex><ql-named-text name="teams-header"></ql-named-text></v-flex>
        </v-layout>
       </v-container>"""

}
object TeamsTitleComponent extends RouteComponent{
   val  template="""
    <v-toolbar      
      color="amber darken-3"
      dark
      dense
      >
      <ql-title>Teams</ql-title>
      <v-toolbar-title class="white--text" >
        Teams 
      </v-toolbar-title>
    </v-toolbar>"""
       


}

object StartTeamPage extends RouteComponent with GridSizeComponentConfig{
    val template="""
      <v-container v-bind="gridSize" fluid>
        <v-layout>
          <v-flex><ql-named-text name="start-team"></ql-named-text></v-flex>
        </v-layout>
       </v-container>"""

}

object StartTeamTitleComponent extends RouteComponent{
   val  template="""
    <v-toolbar      
      color="amber darken-3"
      dark
      >
      <ql-title>Starting a Team</ql-title>
      <v-toolbar-title class="white--text" >
        Starting a Team 
      </v-toolbar-title>
    </v-toolbar>"""
       


}

