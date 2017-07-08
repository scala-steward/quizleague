package quizleague.web.maintain.user

import angulate2.std._
import angular.material.MaterialModule
import angulate2.forms.FormsModule
import angulate2.platformBrowser.BrowserModule
import angulate2.router.{Route,RouterModule}

import scala.scalajs.js
import quizleague.web.model.Venue
import angulate2.http.Http
import quizleague.web.service.user._
import angular.flexlayout.FlexLayoutModule
import quizleague.web.util.UUID
import quizleague.web.names.ComponentNames

import angulate2.ext.classModeScala
import angulate2.common.CommonModule
import rxjs.Observable
import angulate2.router.RouterModule
import quizleague.web.maintain._

@NgModule(
  imports = @@[CommonModule,FormsModule,MaterialModule,RouterModule,FlexLayoutModule, UserRoutesModule],
  declarations = @@[UserComponent,UserListComponent],
  providers = @@[UserService]
   
)
class UserModule

@Routes(
  root = false,
  Route(path = "maintain/user", children = @@@(
    Route(path = "", children = @@@(Route(
      path = ":id",
      component = %%[UserComponent]),
      Route(
        path = "",
        component = %%[UserListComponent]))),
    Route(path = "", component = %%[MaintainMenuComponent], outlet="sidemenu"))))
class UserRoutesModule 

@Injectable
@classModeScala
class UserService(override val http:Http) extends UserGetService with UserPutService with ServiceRoot

