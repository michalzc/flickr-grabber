package michalz.flickrgrabber.model

/**
  * Created by michal on 27.11.16.
  */
case class Photo(id: String, owner: String, secret: String, server: String, farm: Int, title: String, public: Boolean, friend: Boolean, family: Boolean)
