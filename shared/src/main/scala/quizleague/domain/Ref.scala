package quizleague.domain

case class Ref[T <: Entity](typeName:String,id:String, key:Option[Key] = None)