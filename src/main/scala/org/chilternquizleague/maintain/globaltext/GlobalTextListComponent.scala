package org.chilternquizleague.maintain.globaltext

import angulate2.std._
import org.chilternquizleague.web.model._
import scala.scalajs.js
import angulate2.router.Router
import org.chilternquizleague.maintain.component.ListComponent


import angulate2.ext.classModeScala

@Component(
  selector = "ql-globaltext-list",
  template = """
  <div>
    <h2>Global Text</h2>
    <div *ngFor="let item of items">
      <a routerLink="/globalText/{{item.id}}" md-button>{{item.name}}</a>
    </div>
    <div style="position:absolute;right:1em;bottom:5em;">
      <button md-fab (click)="addNew()">
          <md-icon class="md-24">add</md-icon>
      </button>
    </div>
  </div>
  """    
)
@classModeScala
class GlobalTextListComponent (
    override val service:GlobalTextService,
    override val router: Router) 
   extends ListComponent[GlobalText] with OnInit with GlobalTextNames{
  
}