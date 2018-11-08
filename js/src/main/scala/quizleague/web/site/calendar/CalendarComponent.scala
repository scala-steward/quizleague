package quizleague.web.site.calendar

import scala.scalajs.js
import quizleague.web.core._
import quizleague.web.site._
import quizleague.web.site.season.SeasonIdComponent
import com.felstar.scalajs.vue._
import quizleague.web.site.season.SeasonFormatComponent

object CalendarPage extends RouteComponent with NoSideMenu{
  
  val template = """<ql-calendar v-if="season" :seasonId="season.id"></ql-calendar>"""
  components(CalendarComponent)
  subscription("season")(c => CalendarViewService.season)
}



object CalendarComponent extends Component with GridSizeComponentConfig{
  type facade = SeasonIdComponent with VuetifyComponent
  val name = "ql-calendar" 
  val template = """
  <v-container v-bind="gridSize"  v-if="items" class="ql-calendar" fluid >
    <v-timeline v-bind="dense">
      <v-timeline-item v-for="item in items" :key="item.date"  :color="colour(item.events[0].eventType)">
        <v-card>
           <v-card-title :class="colour(item.events[0].eventType)"><h5 class="display-1 white--text font-weight-light">{{item.date | date("EEEE d MMMM yyyy")}}</h5></v-card-title>
           <v-card-text>
            <div v-for="event in item.events">
                <ql-fixtures-event v-if="event.eventType === 'fixtures'" :event="event"></ql-fixtures-event>
                <ql-calendar-event v-if="event.eventType === 'calendar'" :event="event"></ql-calendar-event>
                <ql-competition-event v-if="event.eventType === 'competition'" :event="event"></ql-competition-event>
            </div>
           </v-card-text>
        </v-card>
      </v-timeline-item>
    </v-timeline>
  </v-container>"""
  props("seasonId")
  subscription("items", "seasonId")(c => CalendarViewService.events(c.seasonId))
  components(FixturesEventComponent,CalendarEventComponent,CompetitionEventComponent)
  method("colour"){s:String => (s match {case "fixtures" => "green" case "calendar" => "blue" case "competition" => "purple"}) + " darken-3"}
  def dense(c:facade) = js.Dictionary("dense" -> c.$vuetify.breakpoint.xsOnly)
  computed("dense")({dense _}:js.ThisFunction)
  
}

object CalendarTitleComponent extends RouteComponent with SeasonFormatComponent{
  val template = """
    <v-toolbar      
      color="yellow darken-3"
      dark
      clipped-left
      >
      <ql-title>Calendar {{formatSeason(s)}}</ql-title>
      <v-toolbar-title class="white--text" >
        Calendar
      </v-toolbar-title>
      &nbsp;<h3><ql-season-select :season="season"></ql-season-select></h3>
    </v-toolbar>"""
  
  data("season", CalendarViewService.season)
  subscription("s")(c => CalendarViewService.season)
}


@js.native
trait EventComponent extends VueRxComponent{
  
  val event:EventWrapper
}
  

@js.native
trait PanelComponent extends EventComponent{

  var panelVisible:Boolean

}


trait EventComponentConfig extends Component{
   
   type facade = PanelComponent
   data("panelVisible",false)
   props("event")
   method("togglePanel")({c:facade => c.panelVisible = !c.panelVisible}:js.ThisFunction)
}



object FixturesEventComponent extends EventComponentConfig{
  
  val name = "ql-fixtures-event"
   val template = s"""         
      <v-layout column align-start class="panel-component">
          <v-flex align-start><router-link :to="'/competition/' + event.competition.id + '/' + event.competition.typeName">{{event.fixtures.parentDescription}} {{event.fixtures.description}}</router-link>
            <v-btn icon v-on:click="togglePanel" class="#view-btn">
             <v-icon v-if="!panelVisible">visibility</v-icon>
             <v-icon v-if="panelVisible">visibility_off</v-icon>
            </v-btn>
          </v-flex> 
      <v-flex v-if="panelVisible"><ql-fixtures-simple :fixtures="event.fixtures.fixtures | combine"></ql-fixtures-simple></v-flex>

     </v-layout>
"""

}

object CalendarEventComponent extends EventComponentConfig{
  
  val name = "ql-calendar-event"
  val template = """
    <v-layout column align-start class="panel-component">
      <div v-if="event.event"><b>{{event.event.description}}</b>  {{event.event.time}}  Venue : <router-link v-if="event.event.venue"router-link :to="'/venue/' + event.event.venue.id">{{async(event.event.venue).name}}</router-link></div>
     </v-layout>
      """

}

object CompetitionEventComponent extends EventComponentConfig{
  
  val name = "ql-competition-event"
  val template = """<div><router-link :to="'/competition/' + event.competition.id+'/'+event.competition.typeName">{{event.competition.name}}</router-link>  {{event.event.time}}  Venue : <router-link :to="'/venue/' + event.event.venue.id">{{async(event.event.venue).name}}</router-link></div>"""

}

