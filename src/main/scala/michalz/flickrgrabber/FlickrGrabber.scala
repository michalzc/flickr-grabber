package michalz.flickrgrabber

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import michalz.flickrgrabber.config.FlickGrabberConfig
import michalz.flickrgrabber.stream.FlickrGrabberStream

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

/**
  * Created by michal on 23.11.16.
  */
object FlickrGrabber extends App with LazyLogging {

  logger.info("Starting FlickGrabber")

  implicit val system = ActorSystem("flickr-grabber-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val fgConfig = FlickGrabberConfig(system.settings.config)

  val stream = FlickrGrabberStream(fgConfig, 500)

  val resp = stream.run()
  Await.ready(resp, Duration.Inf).onComplete {
    case Success(bytes) => logger.info(s"$bytes bytes downloaded")
    case Failure(ex) => logger.warn(s"Error during downloading flickr images: $ex")
  }

  shutdown()

  def shutdown() = {
    logger.info("Shutdown called")

    val terminatedFuture = for {
      _ <- Http().shutdownAllConnectionPools()
      _ = materializer.shutdown()
      actorSystemTermination <- system.terminate()
    } yield actorSystemTermination

    Await.ready(terminatedFuture, Duration.Inf)
  }
}
