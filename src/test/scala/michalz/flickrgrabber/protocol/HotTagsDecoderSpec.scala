package michalz.flickrgrabber.protocol

import michalz.flickrgrabber.model.ResponseStatus.Ok
import michalz.flickrgrabber.model.Tag
import org.scalatest.{FunSpec, Matchers}

import scala.util.{Failure, Success}
import scala.xml.XML

/**
 * Created by michal on 03.12.16.
 */
class HotTagsDecoderSpec extends FunSpec with Matchers {

  val decoder = new HotTagsDecoder

  describe("The decoder") {
    it("should deserializer proper response") {
      val xml = XML.load(getClass.getResourceAsStream("/flickr.tags.getHotList.xml"))
      val resp = decoder.fromNodeSeq(xml)
      resp.status should be (Ok)
      resp.content match {
        case Left(errorResponse) => fail(s"Failed with ${errorResponse.code}:${errorResponse.msg}")
        case Right(content) => {
          content.count should === (6)
          content.period should === ("day")
          content.tags should have size (6)
          content.tags should contain (Tag(4, "jan06"))
        }
      }
    }
  }
}
