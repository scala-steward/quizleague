package quizleague.web.service.results

import angulate2.std.Injectable
import angulate2.ext.classModeScala
import angulate2.http.Http
import quizleague.web.service.EntityService
import quizleague.web.model._
import quizleague.web.model.{ Results => Model }
import quizleague.domain.{ Results => Dom }
import quizleague.domain.Ref
import rxjs.Observable
import scala.scalajs.js
import quizleague.web.service._
import java.time.Year
import quizleague.web.util.DateTimeConverters._
import scala.scalajs.js.Date
import quizleague.web.maintain.results.ResultsNames
import quizleague.web.service.fixtures.FixturesGetService


trait ResultsGetService extends GetService[Results] with ResultsNames {
  override type U = Dom
  val resultService:ResultGetService
  val fixturesService:FixturesGetService

  override protected def mapOutSparse(dom: Dom) = Model(dom.id,null,List())
  override protected def mapOut(dom: Dom) = ???

  import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
  import quizleague.util.json.codecs.ScalaTimeCodecs._
  override def deser(jsonString: String) = decode[Dom](jsonString).merge.asInstanceOf[Dom]

}

trait ResultsPutService extends PutService[Results] with ResultsGetService {
  
  override val resultService:ResultPutService
  override protected def mapIn(model: Model) = ???

  override protected def make() = ???

  import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
  import quizleague.util.json.codecs.ScalaTimeCodecs._
  override def ser(item: Dom) = item.asJson.noSpaces

}