package michalz.flickrgrabber.config

import com.typesafe.config.Config

/**
  * Created by michal on 24.11.16.
  */
case class FlickGrabberConfig(apiKey: String, downloadDir: String, nrOfImages: Int)

object FlickGrabberConfig {
  def apply(config: Config): FlickGrabberConfig = {
    val fgConfig = config.getConfig("flickr-grabber")
    FlickGrabberConfig(fgConfig.getString("api-key"), fgConfig.getString("download-dir"), fgConfig.getInt("number-of-images"))
  }
}
