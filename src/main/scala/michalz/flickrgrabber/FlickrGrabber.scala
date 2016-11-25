package michalz.flickrgrabber

import akka.Done
import akka.actor.ActorSystem
import akka.pattern.after
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import michalz.flickrgrabber.config.FlickGrabberConfig
import michalz.flickrgrabber.stream.FlickrGrabberStream

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{Duration, FiniteDuration}

/**
  * Created by michal on 23.11.16.
  */
object FlickrGrabber extends App with LazyLogging {

  logger.info("Starting FlickGrabber")

  implicit val system = ActorSystem("flickr-grabber-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val fgConfig = FlickGrabberConfig(system.settings.config)

  val stream = FlickrGrabberStream(fgConfig)

  val resp = stream.run()
  Await.ready(resp, Duration.Inf)

  shutdown()

  def shutdown() = {
    Thread.sleep(10000)
    logger.info("Shutdown called")

    val terminatedFuture = for {
      _ <- Http().shutdownAllConnectionPools().recover {
        case ex =>
          logger.warn("Exception during shutdown connections", ex)
          ()
      }
      _ <- after(FiniteDuration.apply(1, "second"), system.scheduler)(Future.successful(Done))
      _ = materializer.shutdown()
      actorSystemTermination <- system.terminate()
    } yield actorSystemTermination

    Await.ready(terminatedFuture, Duration.Inf)
  }
}
