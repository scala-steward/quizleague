package quizleague.web.site.season

import angulate2.ext.classModeScala
import angulate2.http.Http
import angulate2.std.Injectable
import quizleague.web.model.Season
import quizleague.web.service.season.SeasonGetService
import quizleague.web.site.ServiceRoot
import quizleague.web.site.competition.CompetitionService
import quizleague.web.site.text.TextService
import quizleague.web.site.venue.VenueService

@Injectable
@classModeScala
class SeasonService(override val http: Http,
    override val textService: TextService,
    override val competitionService: CompetitionService,
    override val venueService: VenueService) extends SeasonGetService with ServiceRoot {
  
    def getResults(season:Season) = get(season.id)(4).map((s,i) => s.competitions.flatMap(c => c.results))
    
}
