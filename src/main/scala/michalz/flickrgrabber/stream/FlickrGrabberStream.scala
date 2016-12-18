package michalz.flickrgrabber.stream

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, ResponseEntity, _}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.Materializer
import akka.stream.scaladsl.{FileIO, Flow, Keep, Sink, Source}
import akka.{Done, NotUsed}
import com.github.slugify.Slugify
import com.typesafe.scalalogging.LazyLogging
import michalz.flickrgrabber.config.FlickGrabberConfig
import michalz.flickrgrabber.model._
import michalz.flickrgrabber.protocol.FlickrApiXmlSupport

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * Created by michal on 24.11.16.
  */
object FlickrGrabberStream extends FlickrApiXmlSupport with LazyLogging {
  val slugify = new Slugify()

  def apply(fgConfig: FlickGrabberConfig, nrOfImages: Int)(implicit system: ActorSystem, materializer: Materializer) = {

    implicit val executionContext = system.dispatcher
    implicit val apiKey = ApiKey(fgConfig.apiKey)

    val httpSearchFlow = Http().superPool[NotUsed]()
    val httpInfoFlow = Http().superPool[PhotoInfo]()

    val requestUri = UriMethods.getHotTags()
    val request = HttpRequest(uri = requestUri)

    val source = Source.single(request -> NotUsed)

    val sink = Sink.fold(0L) { (acc, elem: Long) => acc + elem }

    source
      .via(httpSearchFlow)
      .mapAsync(1)(tup => deserialize[ApiResponse[HotTagsResponse]](tup._1))
      .map(extractTags)
      .via(printErrors)
      .collect { case Right(hotTags) => hotTags }
      .map(hotTags => HttpRequest(uri = UriMethods.getFeaturedPhotos(tags = hotTags.tags.map(_.name), perPage = fgConfig.nrOfImages)) -> NotUsed)
      .via(httpSearchFlow)
      .mapAsync(1)(tup => deserialize[ApiResponse[SearchPhotoResponse]](tup._1))
      .via(extractPhotos)
      .via(printErrors)
      .collect { case Right(photoInfo) => photoInfo }
      .map(mapToSizeRequest)
      .via(httpInfoFlow)
      .mapAsync(1)(tup => deserialize[ApiResponse[PhotoSizesResponse]](tup._1).map(_ -> tup._2))
      .via(extractSizes)
      .via(printErrors)
      .collect { case Right(info) => info }
      .map(info => (HttpRequest(uri = info.url.get)) -> info)
      .via(httpInfoFlow)
      .map { case (tr, info) => clearResponse(tr, info) }
      .via(printErrors)
      .collect { case Right(i) => i }
      .mapAsync(1) { case (ent, info) => downloadImages(ent.withoutSizeLimit(), info) }
      .via(printErrors)
      .collect { case Right(bytes) => bytes }
      .toMat(sink)(Keep.right)
  }

  def extractTags(ent: Either[Throwable, ApiResponse[HotTagsResponse]]): Either[String, HotTagsResponse] = {
    ent.left.map(ex => s"Failure during getting hot tags: $ex").flatMap(resp => resp.content.left.map(resp => s"Failure during extracting hot tags: ${resp.code} => ${resp.msg}"))
  }

  def downloadImages(entity: ResponseEntity, photoInfo: PhotoInfo)(implicit ec: ExecutionContext, mat: Materializer): Future[Either[String, Long]] = {
    val fileName = photoInfo.fileName.get
    val path = Paths.get("images", fileName)
    val sink = FileIO.toPath(path)
    entity.dataBytes.runWith(sink).map { res =>
      res.status match {
        case Success(Done) => Right(res.count)
        case Failure(ex) => Left(s"Error during saving image ${fileName}: $ex")
      }
    }.recover {
      case ex: Throwable => Left(s"Error during downloading image ${photoInfo.url}: $ex")
    }
  }

  def clearResponse(resp: Try[HttpResponse], photoInfo: PhotoInfo)(implicit materializer: Materializer): Either[String, (ResponseEntity, PhotoInfo)] = resp match {
    case Success(response) => response.status match {
      case StatusCodes.OK => Right(response.entity -> photoInfo)
      case unknown =>
        Left(s"Unknown response code $unknown for image ${photoInfo.url}")
    }
    case Failure(ex) => Left(s"Error during getting image: ${photoInfo.url}, $ex")
  }

  val extractSizes = Flow[(Either[Throwable, ApiResponse[PhotoSizesResponse]], PhotoInfo)].map {
    case (eitherResp, photoInfo) =>
      eitherResp.left.map(ex => s"Exception during serializarion: $ex").flatMap { resp =>
        resp.content.left.map(er => s"Api error: ${er.code}: ${er.msg}").flatMap { sizes =>
          val biggest = sizes.maxSize
          if (biggest.width * biggest.height < 600 * 400) Left(s"Biggest image ${photoInfo.id} is too small (${biggest.width} x ${biggest.height})")
          else Right(photoInfo.copy(url = Some(biggest.source)))
        }
      }
  }

  def deserialize[T](tr: Try[HttpResponse])(implicit executionContext: ExecutionContext, um: Unmarshaller[ResponseEntity, T], materializer: Materializer): Future[Either[Throwable, T]] = tr match {
    case Success(response) if (response.status == StatusCodes.OK) =>
      Unmarshal(response.entity).to[T].map(Right.apply).recover { case e: Throwable => Left(e) }

    case Failure(ex) =>
      Future.successful(Left(ex))
  }

  def printErrors[T] = Flow[Either[String, T]].map { elem =>
    elem match {
      case Right(_) => ()
      case Left(ex) => logger.warn(s"Problem $ex", ex)
    }
    elem
  }

  val extractPhotos = Flow[Either[Throwable, ApiResponse[SearchPhotoResponse]]].mapConcat {
    case Left(ex) => List(Left(s"Exception during request: $ex"))
    case Right(response) => response.content match {
      case Left(apiError) => List(Left(s"Api error: ${apiError.code}: ${apiError.msg}"))
      case Right(rsp) => rsp.photos.map { photo =>
        Right(PhotoInfo(photo.id, s"${photo.id}-${slugify.slugify(photo.title)}"))
      }
    }
  }

  def mapToSizeRequest(photoInfo: PhotoInfo)(implicit apiKey: ApiKey) = {
    (HttpRequest(uri = UriMethods.getPhotoSizes(photoInfo.id)) -> photoInfo)
  }

}
