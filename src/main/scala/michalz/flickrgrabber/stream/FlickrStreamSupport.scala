package michalz.flickrgrabber.stream

import akka.http.scaladsl.model.{HttpResponse, ResponseEntity, StatusCodes}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
 * Created by michal on 18.12.16.
 */
trait FlickrStreamSupport extends LazyLogging {

  def deserializeFlow[T, O](parallelism: Int)(implicit um: Unmarshaller[ResponseEntity, T], executionContext: ExecutionContext, materializer: Materializer) =
    Flow[(Try[HttpResponse], O)].mapAsync(parallelism) {
      case (Success(response), other) if response.status == StatusCodes.OK =>
        Unmarshal(response.entity).to[T].map(ResponseTuple(_, other))

      case (Success(response), other) =>
        val f = response.discardEntityBytes().future()
        f.flatMap(_ => Future.failed(new IllegalStateException(s"Unknown response: ${response.status}")))

      case (Failure(ex), other) =>
        Future.failed(ex)
    }

}
