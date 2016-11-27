package michalz.flickrgrabber.model

/**
  * Created by michal on 27.11.16.
  */
case class PhotoSizesResponse(download: Boolean, print: Boolean, blog: Boolean, sizes: List[PhotoSize]) {
  def maxSize = sizes.maxBy(s => s.width * s.height)
}