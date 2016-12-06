package michalz.flickrgrabber.protocol

import michalz.flickrgrabber.model.{HotTagsResponse, Tag}

import scala.xml.{Node, NodeSeq}

/**
  * Created by michal on 03.12.16.
  */
class HotTagsDecoder extends FlickrApiDecoder[HotTagsResponse]{

  def parseTag(tagElem: Node): Tag = {
    Tag((tagElem \@ "score").toInt, tagElem.text)
  }

  override protected def contentDecoder(ns: NodeSeq): HotTagsResponse = {
    val hotTags = (ns \ "hottags")(0)
    HotTagsResponse(hotTags \@ "period", (hotTags \@ "count").toInt, (hotTags \ "tag").toList.map( tagElem => parseTag(tagElem)))
  }
}
