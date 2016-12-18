package michalz.flickrgrabber.config

import com.typesafe.config.Config
import michalz.flickrgrabber.model.ApiKey

/**
 * Created by michal on 24.11.16.
 */
case class FlickrGrabberConfig(apiKey: ApiKey, downloadDir: String, nrOfImages: Int, nrOfTags: Int, decoderParallelism: Int)

object FlickrGrabberConfig {
  def apply(config: Config): FlickrGrabberConfig = {
    val fgConfig = config.getConfig("flickr-grabber")
    FlickrGrabberConfig(
      ApiKey(fgConfig.getString("api-key")),
      fgConfig.getString("download-dir"),
      fgConfig.getInt("number-of-images"),
      fgConfig.getInt("number-of-tags"),
      fgConfig.getInt("decoder-parallelism")
    )
  }
}
