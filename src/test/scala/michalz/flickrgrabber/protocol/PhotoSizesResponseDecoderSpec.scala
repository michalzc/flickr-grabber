package michalz.flickrgrabber.protocol

import michalz.flickrgrabber.model.ResponseStatus.Ok
import org.scalatest.{FunSpec, Matchers}

import scala.xml.XML

/**
  * Created by michal on 27.11.16.
  */
class PhotoSizesResponseDecoderSpec extends FunSpec with Matchers {

  val decoder = new PhotoSizesResponseDecoder()

  describe("The decoder") {
    it("should deserialize the proper response") {
      val xml = XML.load(getClass.getResourceAsStream("/getSizesResponse.xml"))
      val resp = decoder.fromNodeSeq(xml)

      resp.status should be (Ok)
      resp.content match {
        case Left(err) => fail(s"Failed response")
        case Right(sizes) =>
          sizes.sizes should not be empty
      }
    }
  }

}
