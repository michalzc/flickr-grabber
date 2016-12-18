package michalz.flickrgrabber.stream

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink}
import michalz.flickrgrabber.config.FlickrGrabberConfig

import scala.concurrent.ExecutionContext

/**
 * Created by michal on 18.12.16.
 */
object FlickrGrabberStream {

  def apply()(implicit fgConfig: FlickrGrabberConfig, system: ActorSystem, materializer: Materializer, executionContext: ExecutionContext) = {
    val src = PopularTagsGraph()
    val sink = Sink.ignore

    src
      .log("TAGS")
      .toMat(sink)(Keep.right)
  }
}
