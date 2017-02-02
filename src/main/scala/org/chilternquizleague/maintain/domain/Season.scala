package org.chilternquizleague.maintain.domain

import java.time.Year

case class Season(
    id:String,
    startYear:Year,
    endYear:Year,
    text:Ref[Text],
    competitions:List[Ref[User]],
    retired:Boolean = false
) extends Entity