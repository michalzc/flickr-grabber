package michalz.flickrgrabber.model

import akka.http.scaladsl.model.Uri

/**
  * Created by michal on 27.11.16.
  */
object UriMethods {

  val apiUrl = Uri("https://api.flickr.com/services/rest/")

  def getFeaturedPhotos(perPage: Int = 10)(implicit apiKey: ApiKey) = {
    apiUrl.withQuery(Uri.Query(
      "method" -> "flickr.photos.search",
      "api_key" -> apiKey.key,
      "tags" -> "featured",
      "sort" -> "interestingness-asc",
      "per_page" -> perPage.toString
    ))
  }

  def getPhotoSizes(photoId: String)(implicit apiKey: ApiKey) = {
    apiUrl.withQuery(Uri.Query(
      "method" -> "flickr.photos.getSizes",
      "api_key" -> apiKey.key,
      "photo_id" -> photoId
    ))
  }

}

case class ApiKey(key: String)
