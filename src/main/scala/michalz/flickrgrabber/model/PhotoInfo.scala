package michalz.flickrgrabber.model

/**
  * Created by michal on 27.11.16.
  */
case class PhotoInfo(id: String, slug: String, url: Option[String]) {
  def fileName: Option[String] = url.map { u =>
    val ext = u.split("\\.").last
    s"$slug.$ext"
  }
}

object PhotoInfo {
  def apply(id: String, slug: String): PhotoInfo = PhotoInfo(id, slug, None)
}
