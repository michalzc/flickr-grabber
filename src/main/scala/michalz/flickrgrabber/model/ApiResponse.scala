package michalz.flickrgrabber.model

/**
  * Created by michal on 24.11.16.
  */
case class ApiResponse[T] (status: ResponseStatus, content: Either[ErrorResponse, T])


case class ErrorResponse(code: String, msg: String)