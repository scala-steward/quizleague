package quizleague.web.maintain.competition

import angulate2.std._
import angulate2.router.ActivatedRoute
import angulate2.common.Location
import quizleague.web.maintain.component.ItemComponent
import quizleague.web.maintain.component._
import quizleague.web.model._
import scala.scalajs.js
import angulate2.ext.classModeScala
import TemplateElements._
import quizleague.web.maintain.text.TextService
import angulate2.router.Router
import js.Dynamic.{ global => g }
import angulate2.core.Input
import quizleague.web.maintain.text.TextEditMixin
import quizleague.web.util.Logging
import quizleague.web.maintain.text.TextEditMixin
import angulate2.router.Router
import quizleague.web.maintain.venue.VenueService


@Component(
  template = s"""
  <div>
    <h2>Singleton Competition Detail</h2>
    <form #fm="ngForm" (submit)="save()">
      <div fxLayout="column">
        <md-input-container>
          <input mdInput placeholder="Name" type="text"
             required
             [(ngModel)]="item.name" name="name">
        </md-input-container>
          <div fxLayout="row">
            <md-input-container>        
              <input mdInput placeholder="Date" type="date"
                 required
                 [(ngModel)]="item.event.date" name="date{{i}}">
            </md-input-container>
            <md-input-container>        
              <input mdInput placeholder="Time" type="time"
                 required
                 [(ngModel)]="item.event.time" name="time{{i}}">
            </md-input-container>
            <md-input-container>        
              <input mdInput placeholder="Duration" type="number" step="0.1"
                 required
                 [(ngModel)]="item.event.duration" name="duration{{i}}">
            </md-input-container>
          </div>
            <md-select placeholder="Venue" name="venue{{i}}" [(ngModel)]="item.event.venue" required >
              <md-option *ngFor="let venue of venues" [value]="venue" >
                {{venue.name}}
              </md-option>
            </md-select>
       </div>
      <div fxLayout="row"><button (click)="editText(item.text)" md-button type="button" >Edit Text...</button></div>
      $formButtons
    </form>
  </div>

  """    
)
@classModeScala
class SingletonCompetitionComponent( override val service:CompetitionService,
                                  override val location:Location,
                                  override val route:ActivatedRoute,
                                  override val router:Router,
                                  val venueService:VenueService) extends CompetitionComponent{
  
  var venues:js.Array[Venue] = _
  
  override def init() = {
    venueService.list().subscribe(venues = _)
    super.init()
  }
}