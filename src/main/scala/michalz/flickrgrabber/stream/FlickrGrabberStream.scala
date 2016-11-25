package michalz.flickrgrabber.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, ResponseEntity, Uri}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import com.typesafe.scalalogging.LazyLogging
import michalz.flickrgrabber.config.FlickGrabberConfig

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scala.xml.NodeSeq

/**
 * Created by michal on 24.11.16.
 */
object FlickrGrabberStream extends ScalaXmlSupport with LazyLogging {
  def apply(fgConfig: FlickGrabberConfig)(implicit system: ActorSystem, materializer: Materializer) = {

    implicit val executionContext = system.dispatcher

    val httpFlow = Http().superPool[Int]()

    val requestUri = Uri("https://api.flickr.com/services/rest/").withQuery(Uri.Query(
      "api_key" -> fgConfig.apiKey,
      "method" -> "flickr.photos.search",
      "tags" -> "featured",
      "sort" -> "interestingness-asc",
      "per_page" -> "10"
    ))
    val request = HttpRequest(uri = requestUri)

    val source = Source.single(request -> 42)

    source
      .via(httpFlow)
      .mapAsync(1)(tup => deserialize[NodeSeq](tup._1))
      .via(printFlow)
      .toMat(Sink.ignore)(Keep.right)
  }

  def deserialize[T](tr: Try[HttpResponse])(implicit executionContext: ExecutionContext, um: Unmarshaller[ResponseEntity, T], materializer: Materializer): Future[Either[Throwable, T]] = tr match {
    case Success(r) =>
      Unmarshal(r.entity).to[T].map(Right.apply).recover { case e: Throwable => Left(e) }

    case Failure(ex) =>
      Future.successful(Left(ex))
  }

  def printFlow[T] = Flow[Either[Throwable, T]].map { elem =>
    elem match {
      case Right(e) => logger.info(s"Elem:\n$e")
      case Left(ex) => logger.warn(s"Exception $ex", ex)
    }
    elem
  }

}
