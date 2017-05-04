package quizleague.web.service.applicationcontext

import scala.scalajs.js

import quizleague.domain.{ ApplicationContext => Dom, EmailAlias => DomEmailAlias }
import quizleague.web.model.{ ApplicationContext, EmailAlias, GlobalText, Season, User }
import quizleague.web.names.ApplicationContextNames
import quizleague.web.service.{ GetService, PutService }
import quizleague.web.service.globaltext.{ GlobalTextGetService, GlobalTextPutService }
import quizleague.web.service.season.{ SeasonGetService, SeasonPutService }
import quizleague.web.service.user.{ UserGetService, UserPutService }
import quizleague.web.util.Logging
import rxjs.Observable

trait ApplicationContextGetService extends GetService[ApplicationContext] with ApplicationContextNames with Logging {
  override type U = Dom

  val globalTextService: GlobalTextGetService
  val userService: UserGetService
  val seasonService: SeasonGetService
  override protected def mapOutSparse(context: Dom) = ApplicationContext(context.id, context.leagueName, null, null, context.senderEmail, js.Array())
  override protected def mapOut(context: Dom)(implicit depth: Int) =
    Observable.zip(
      child(context.textSet, globalTextService),
      child(context.currentSeason, seasonService),
      mapOutAliases(context.emailAliases),
      (textSet: GlobalText, currentSeason: Season, emailAliases: js.Array[EmailAlias]) => log(ApplicationContext(context.id, context.leagueName, textSet, currentSeason, context.senderEmail, emailAliases), "mapOut ApplicationContext"))

  def mapOutAliases(list: List[DomEmailAlias])(implicit depth: Int): Observable[js.Array[EmailAlias]] =
    Observable.zip(list.map((e: DomEmailAlias) => child(e.user, userService).map((u: User, i: Int) => EmailAlias(e.alias, u))): _*)

  def listTextSets() = globalTextService.list()

  def get(): Observable[ApplicationContext] = list().switchMap((x, i) => get(x(0).id))

  import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

  override def deser(jsonString: String) = decode[Dom](jsonString).merge.asInstanceOf[Dom]

}

trait ApplicationContextPutService extends PutService[ApplicationContext] with ApplicationContextGetService {
  override val globalTextService: GlobalTextPutService
  override val userService: UserPutService
  override val seasonService: SeasonPutService

  override protected def mapIn(context: ApplicationContext) = Dom(context.id, context.leagueName, globalTextService.getRef(context.textSet), seasonService.getRef(context.currentSeason), context.senderEmail, context.emailAliases.map(ea => DomEmailAlias(ea.alias, userService.getRef(ea.user))).toList)
  override protected def make() = Dom(newId(), "", null, null, "", List())

  import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

  override def ser(item: Dom) = item.asJson.noSpaces

}
