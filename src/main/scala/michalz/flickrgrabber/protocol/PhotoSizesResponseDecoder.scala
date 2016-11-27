package michalz.flickrgrabber.protocol

import michalz.flickrgrabber.model.{PhotoSize, PhotoSizesResponse}

import scala.xml.{Node, NodeSeq}

/**
  * Created by michal on 27.11.16.
  */
class PhotoSizesResponseDecoder extends FlickrApiDecoder[PhotoSizesResponse] {

  override protected def contentDecoder(ns: NodeSeq): PhotoSizesResponse = {
    val sizes = (ns \ "sizes")(0)
    PhotoSizesResponse(
      decodeBool(sizes \@ "candownload"),
      decodeBool(sizes \@ "canprint"),
      decodeBool(sizes \@ "canblog"),
      (sizes \ "size").toList.map(decodeSize)
    )
  }

  def decodeSize(node: Node): PhotoSize = PhotoSize(
    node \@ "label",
    (node \@ "width").toInt,
    (node \@ "height").toInt,
    node \@ "source",
    node \@ "url",
    node \@ "media"
  )
}
