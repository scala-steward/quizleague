package org.chilternquizleague.maintain.applicationcontext

import angulate2.std._
import angulate2.router.ActivatedRoute
import angulate2.common.Location
import org.chilternquizleague.maintain.component.ItemComponent
import org.chilternquizleague.maintain.component._
import org.chilternquizleague.maintain.model._
import scala.scalajs.js
import org.chilternquizleague.maintain.venue.VenueService
import angulate2.ext.classModeScala
import TemplateElements._
import org.chilternquizleague.maintain.text.TextService
import angulate2.router.Router
import js.Dynamic.{ global => g }
import org.chilternquizleague.maintain.model.GlobalText

@Component(
  selector = "ql-application-context",
  template = s"""
  <div>
    <h2>Application Context</h2>
    <form #fm="ngForm" (submit)="save()">
      <div fxLayout="column">
        <md-input placeholder="League Name" type="text"
             required
             [(ngModel)]="item.leagueName" name="leagueName">
        </md-input>
        <md-select placeholder="Global Text" name="globalText" [(ngModel)]="item.textSet" required >
          <md-option *ngFor="let textSet of textSets" [value]="textSet" >
            {{textSet.name}}
          </md-option>
        </md-select>
        <md-input placeholder="Sender Email" type="text"
             required
             [(ngModel)]="item.senderEmail" name="senderEmail">
        </md-input>
        <label style="color: rgba(0,0,0,.38);">Email Aliases</label>
        <md-chip-list>
          <md-chip *ngFor="let alias of item.emailAliases">{{alias.alias}} : {{alias.user.name}}</md-chip> 
        </md-chip-list>
     </div>
     $formButtons
    </form>
  </div>
  """    
)
@classModeScala
class ApplicationContextComponent(
    override val service:ApplicationContextService,
    override val route: ActivatedRoute,
    override val location:Location,
    val router:Router)
    extends ItemComponent[ApplicationContext] {
  
  var textSets:js.Array[GlobalText] = _
  
  override def ngOnInit() = {super.ngOnInit();initTextSets}
  
  override def init() = service.get.subscribe(item = _)
  
  private def initTextSets() = service.listTextSets.subscribe(textSets = _)
  
 
}
    