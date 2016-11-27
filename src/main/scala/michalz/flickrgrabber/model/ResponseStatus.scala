package michalz.flickrgrabber.model

/**
  * Created by michal on 27.11.16.
  */
sealed trait ResponseStatus

object ResponseStatus {
  case object Ok extends ResponseStatus
  case object Fail extends ResponseStatus

  def apply(code: String): ResponseStatus = code match {
    case "ok" => Ok
    case "fail" => Fail
  }
}
