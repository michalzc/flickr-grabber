package michalz.flickrgrabber.protocol

import michalz.flickrgrabber.model.ResponseStatus.{Fail, Ok}
import michalz.flickrgrabber.model.{ApiResponse, ErrorResponse, ResponseStatus}

import scala.xml.{Node, NodeSeq}

/**
  * Created by michal on 27.11.16.
  */
trait FlickrApiDecoder[T] {

  def fromNodeSeq(ns: NodeSeq): ApiResponse[T] = {
    ResponseStatus(ns \@ "stat") match {
      case ResponseStatus.Ok =>
        ApiResponse(Ok, Right(contentDecoder(ns)))

      case ResponseStatus.Fail =>
        val failNode = (ns \ "err")(0)
        ApiResponse(Fail, Left(ErrorResponse(
          failNode \@ "code",
          failNode \@ "msg"
        )))
    }
  }

  protected def contentDecoder(ns: NodeSeq): T

  protected def decodeBool(s: String): Boolean = s match {
    case "1" => true
    case "0" => false
  }
}

object FlickrApiDecoder {
  implicit val searchPhotoResponseDecoder = new SearchPhotoResponseDecoder
  implicit val photoSizesResponseDecoder = new PhotoSizesResponseDecoder
}
