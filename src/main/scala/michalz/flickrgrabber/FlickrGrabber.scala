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

  val stream = FlickrGrabberStream(fgConfig)


  val resp = stream.run()

  Await.ready(resp, Duration.Inf).foreach {
    case Left(ex) => logger.warn("error", ex)
    case Right(nodes) => logger.info(s"Response: $nodes")
  }



  sys.addShutdownHook {
    shutdown()
  }


  shutdown()
  logger.info("Finished")


  def shutdown() = {
    val endFuture = Http().shutdownAllConnectionPools().recover { case _ => ()}.flatMap(_ => system.terminate() )
    Await.ready(endFuture, Duration.Inf)
  }
}
