package michalz.flickrgrabber.stream

import akka.NotUsed
import akka.http.scaladsl.model.HttpRequest

/**
  * Created by michal on 18.12.16.
  */
case class RequestTuple[T](request: HttpRequest, other: T) {
  def tup: (HttpRequest, T) = (request, other)
}

object RequestTuple {
  def apply(request: HttpRequest): RequestTuple[NotUsed] = RequestTuple(request, NotUsed)
}