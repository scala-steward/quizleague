package org.chilternquizleague.maintain.globaltext

import angulate2.std.Injectable
import angulate2.ext.classModeScala
import angulate2.http.Http
import org.chilternquizleague.maintain.service.EntityService
import org.chilternquizleague.maintain.model._
import org.chilternquizleague.maintain.domain.{GlobalText => Dom}
import org.chilternquizleague.maintain.domain.{Ref => DomRef}
import org.chilternquizleague.maintain.domain.{Text => DomText}
import rxjs.Observable
import org.chilternquizleague.maintain.text.TextService

@Injectable
@classModeScala
class GlobalTextService(override val http:Http, val textService:TextService) extends EntityService[GlobalText] with GlobalTextNames{
  override type U = Dom
  
  override protected def mapIn(globalText:GlobalText) = Dom(globalText.id, globalText.name, globalText.text.map{case(k,v) => (k, DomRef[DomText](v.id, v.typeName))}, globalText.retired)
  override protected def mapOut(globalText:Dom):Observable[GlobalText] = Observable.of(
     GlobalText(globalText.id, globalText.name, globalText.text.map {case (k,v) => (k, Ref(v.id, v.typeName))}, globalText.retired)
  
  )
  override protected def mapOutSparse(globalText:Dom):GlobalText =
    GlobalText(globalText.id, globalText.name, Map(), globalText.retired)
  
  override protected def make():Dom = Dom(newId(),"", Map())
  
  import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
  override def ser(item:Dom) = item.asJson.noSpaces
  override def deser(jsonString:String) = decode[Dom](jsonString).merge.asInstanceOf[Dom]

}