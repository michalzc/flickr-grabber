package michalz.flickrgrabber.model

/**
  * Created by michal on 27.11.16.
  */
case class SearchPhotoResponse(page: Int, pages: Int, perPage: Int, total: Int, photos: List[Photo])
