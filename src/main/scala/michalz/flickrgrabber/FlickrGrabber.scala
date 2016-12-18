package michalz.flickrgrabber

import java.nio.file.{Files, Paths}

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.stream.Supervision.Decider
import com.typesafe.scalalogging.LazyLogging
import michalz.flickrgrabber.config.FlickGrabberConfig
import michalz.flickrgrabber.stream.FlickrGrabberStream

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{Duration, DurationInt}
import scala.util.{Failure, Success}
import akka.pattern.after

/**
  * Created by michal on 23.11.16.
  */
object FlickrGrabber extends App with LazyLogging {

  logger.info("Starting FlickGrabber")

  implicit val system = ActorSystem("flickr-grabber-system")
  implicit val materializer = createMaterializer
  implicit val executionContext = system.dispatcher

  val fgConfig = FlickGrabberConfig(system.settings.config)


  checkAndCreateDirectory(fgConfig.downloadDir)
  val stream = FlickrGrabberStream(fgConfig, fgConfig.nrOfImages)

  val resp = stream.run()
  resp.onComplete {
    case Success(bytes) => logger.info(s"$bytes bytes downloaded")
    case Failure(ex) => logger.warn(s"Error during downloading flickr images: $ex")
  }

  Await.ready(resp, Duration.Inf)

  shutdown()

  def shutdown() = {
    logger.info("Shutdown called")


    val downFuture = for {
      connectionPoolDown <- Http().shutdownAllConnectionPools()
      systemDown <- system.terminate()
    } yield systemDown

    downFuture.onComplete {
      case Success(down) => logger.info("Application down: {}", down)
      case Failure(ex) => logger.warn("Error during shutting down", ex)
    }

    Await.ready(downFuture, 30.seconds)
  }

  def checkAndCreateDirectory(dir: String): Unit = {
    val path = Paths.get(dir)
    if(!(Files.exists(path) && Files.isDirectory(path))) {
      Files.createDirectory(path)
    }
  }

  def createMaterializer: ActorMaterializer = {
    val decider: Decider = {
      case ex: Throwable =>
        logger.warn("Exception in stream", ex)
        Supervision.Resume
    }

    ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))
  }
}
