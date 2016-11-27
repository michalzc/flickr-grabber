package michalz.flickrgrabber.protocol

import michalz.flickrgrabber.model.{Photo, SearchPhotoResponse}

import scala.xml.{Node, NodeSeq}

/**
  * Created by michal on 27.11.16.
  */
class SearchPhotoResponseDecoder extends FlickrApiDecoder[SearchPhotoResponse] {

  override protected def contentDecoder(ns: NodeSeq): SearchPhotoResponse = {
    val photosNode = (ns \ "photos")(0)
    SearchPhotoResponse(
      (photosNode \@ "page").toInt,
      (photosNode \@ "pages").toInt,
      (photosNode \@ "perpage").toInt,
      (photosNode \@ "total").toInt,
      (photosNode \ "photo").toList.map(decodePhoto)
    )
  }

  def decodePhoto(node: Node): Photo = Photo(
    node \@ "id",
    node \@ "owner",
    node \@ "secret",
    node \@ "server",
    (node \@ "farm").toInt,
    node \@ "title",
    decodeBool(node \@ "ispublic"),
    decodeBool(node \@ "isfriend"),
    decodeBool(node \@ "isfamily")
  )

}
