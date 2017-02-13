package org.chilternquizleague.web.service.venue

import angulate2.std.Injectable
import angulate2.ext.classModeScala
import angulate2.http.Http
import org.chilternquizleague.web.service.EntityService
import org.chilternquizleague.web.model.Venue
import org.chilternquizleague.domain.{ Venue => DomVenue }
import rxjs.Observable
import org.chilternquizleague.web.service.GetService
import org.chilternquizleague.web.maintain.venue.VenueNames
import org.chilternquizleague.web.service.PutService



trait VenueGetService extends GetService[Venue] with VenueNames {
  override type U = DomVenue
  override protected def mapOut(venue: DomVenue): Observable[Venue] = Observable.of(mapOutSparse(venue))
  override protected def mapOutSparse(venue: DomVenue): Venue = {
    Venue(venue.id, venue.name, venue.phone.getOrElse(null), venue.email.getOrElse(null), venue.website.getOrElse(null), venue.imageURL.getOrElse(null), venue.retired)
  }

  import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

  override def deser(jsonString: String) = decode[DomVenue](jsonString).merge.asInstanceOf[DomVenue]

}

trait VenuePutService extends VenueGetService with PutService[Venue] {
  override type U = DomVenue
  override protected def make(): DomVenue = DomVenue(newId(), "", None, None, None, None)

  override protected def mapIn(venue: Venue) = {
    DomVenue(venue.id, venue.name, Option(venue.phone), Option(venue.email), Option(venue.website), Option(venue.imageURL), venue.retired)
  }
  import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
  override def ser(item: DomVenue) = item.asJson.noSpaces

}