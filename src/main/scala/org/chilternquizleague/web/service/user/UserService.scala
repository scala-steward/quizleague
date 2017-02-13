package org.chilternquizleague.web.service.user

import angulate2.std.Injectable
import angulate2.ext.classModeScala
import angulate2.http.Http
import org.chilternquizleague.web.service._
import org.chilternquizleague.web.model.User
import org.chilternquizleague.domain.{ User => DomUser }
import rxjs.Observable
import org.chilternquizleague.web.maintain.user.UserNames



trait UserGetService extends GetService[User] with UserNames {
  override type U = DomUser

  override protected def mapOut(user: DomUser): Observable[User] = Observable.of(mapOutSparse(user))
  override protected def mapOutSparse(user: DomUser): User =
    User(user.id, user.name, user.email, user.retired)

  import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
  override def deser(jsonString: String) = decode[DomUser](jsonString).merge.asInstanceOf[DomUser]
}

trait UserPutService extends PutService[User] with UserGetService {
  override protected def mapIn(user: User) = DomUser(user.id, user.name, user.email, user.retired)

  override protected def make(): DomUser = DomUser(newId(), "", "")

  import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
  override def ser(item: DomUser) = item.asJson.noSpaces

}