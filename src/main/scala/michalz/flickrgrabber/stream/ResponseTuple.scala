package michalz.flickrgrabber.stream

/**
  * Created by michal on 18.12.16.
  */
case class ResponseTuple[T1, T2](responseData: T1, otherData: T2)