package michalz.flickrgrabber.protocol

import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import michalz.flickrgrabber.model.ApiResponse

import scala.xml.NodeSeq

/**
 * Created by michal on 27.11.16.
 */
trait FlickrApiXmlSupport extends ScalaXmlSupport {

  import FlickrApiDecoder._

  implicit def flickrApiUnmarshaller[T](implicit nsu: FromEntityUnmarshaller[NodeSeq], proto: FlickrApiDecoder[T]): FromEntityUnmarshaller[ApiResponse[T]] = {
    nsu.map(proto.fromNodeSeq)
  }
}

object FlickrApiXmlSupport extends FlickrApiXmlSupport