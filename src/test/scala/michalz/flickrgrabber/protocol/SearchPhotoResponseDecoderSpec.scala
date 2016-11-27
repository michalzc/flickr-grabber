package michalz.flickrgrabber.protocol

import michalz.flickrgrabber.model.{Photo, ResponseStatus}
import org.scalatest.{FunSpec, Matchers}

import scala.xml.{NodeSeq, XML}

/**
  * Created by michal on 27.11.16.
  */
class SearchPhotoResponseDecoderSpec extends FunSpec with Matchers {

  val decoder = new SearchPhotoResponseDecoder()

  describe("The Decoder") {
    it("should deserialize proper xml response") {
      val xml = loadXml("/searchPhotoResponse.xml")

      val resp = decoder.fromNodeSeq(xml)

      resp.status should be (ResponseStatus.Ok)
      resp.content match {
        case Left(er) => fail(s"Failed with error response: $er")
        case Right(photos) =>
          photos.photos should not be empty
          photos.photos should contain(Photo("4488623927", "48972872@N02", "4fa2272dc6", "4045", 5, "Pt Lobos red moss trees against sea hz", true, false, false))
      }
    }
  }

  def loadXml(resName: String): NodeSeq = {
    XML.load(getClass.getResourceAsStream(resName))
  }
}
