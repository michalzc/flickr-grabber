package michalz.flickrgrabber.model

import akka.http.scaladsl.model.Uri

/**
  * Created by michal on 27.11.16.
  */
object UriMethods {

  val apiUrl = Uri("https://api.flickr.com/services/rest/")

  def getHotTags(count: Int = 20)(implicit apiKey: ApiKey) = {
    apiUrl.withQuery(Uri.Query(
      "method" -> "flickr.tags.getHotList",
      "api_key" -> apiKey.key,
      "count" -> count.toString,
      "period" -> "week"
    ))
  }

  def getFeaturedPhotos(perPage: Int = 10, tags: List[String] = List.empty)(implicit apiKey: ApiKey) = {
    apiUrl.withQuery(Uri.Query(
      "method" -> "flickr.photos.search",
      "api_key" -> apiKey.key,
      "tags" -> (if(tags.isEmpty) "featured" else tags.mkString(",")),
      "per_page" -> perPage.toString,
      "content_type" -> "1",
      "sort" -> " interestingness-desc"
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
