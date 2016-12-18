package michalz.flickrgrabber.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.Connection
import akka.stream.scaladsl.{Flow, GraphDSL, Source}
import akka.stream.{Graph, Materializer, SourceShape}
import michalz.flickrgrabber.config.FlickrGrabberConfig
import michalz.flickrgrabber.model.{ApiResponse, HotTagsResponse, Tag, UriMethods}
import michalz.flickrgrabber.protocol.FlickrApiXmlSupport

import scala.concurrent.ExecutionContext

/**
 * Created by michal on 18.12.16.
 */
object PopularTagsGraph extends FlickrApiXmlSupport with FlickrStreamSupport {

  def apply()(implicit fgConfig: FlickrGrabberConfig, system: ActorSystem, executionContext: ExecutionContext, materializer: Materializer): Source[List[Tag], NotUsed] = Source.fromGraph(GraphDSL.create() {
    implicit builder =>
      import GraphDSL.Implicits._

      val request = HttpRequest(uri = UriMethods.getHotTags(fgConfig.nrOfTags)(fgConfig.apiKey))
      val source = Source.single(RequestTuple(request).tup)

      val httpFlow = Http().superPool[NotUsed]()
      val deserializer = deserializeFlow[ApiResponse[HotTagsResponse], NotUsed](fgConfig.decoderParallelism)
      val responseFilter = Flow[ResponseTuple[ApiResponse[HotTagsResponse], NotUsed]].map {
        case ResponseTuple(ApiResponse(status, Right(hotTags)), _) =>
          Some(hotTags)

        case ResponseTuple(ApiResponse(status, Left(problem)), _) =>
          logger.warn("Error response from API: {}", problem)
          None
      }.collect { case Some(r) => r }

      val hotTagsFlow = builder.add(Flow[HotTagsResponse].map(processHotTags))

      source ~> httpFlow ~> deserializer ~> responseFilter ~> hotTagsFlow

      SourceShape(hotTagsFlow.out)
  })

  def processHotTags(hotTags: HotTagsResponse) = {
    logger.debug(s"Received hot tags from api, $hotTags")
    hotTags.tags
  }

}
