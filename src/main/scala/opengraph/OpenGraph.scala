package opengraph

import dispatch.{Http, url}
import model.{OpenGraphKeys, Basic, OpenGraphElement, OpenGraphRequest}
import org.jsoup.Jsoup

import scala.util.{Failure, Success}
import scalaz.concurrent.Task
import scalaz.{-\/, Maybe, \/-}
import scala.collection.JavaConversions._
import scalaz._
import Scalaz._

object OpenGraph {

  import scala.concurrent.ExecutionContext.Implicits.global

  def getUnsafe(request: OpenGraphRequest): Maybe[OpenGraphElement] = getTask(request).run

  def getTask(request: OpenGraphRequest): Task[Maybe[OpenGraphElement]] =
    Task.async { completeWithEither =>
      Http.apply(url(request.url)).onComplete {
        case Success(response) => completeWithEither(\/-(fromBody(response.getResponseBody)))
        case Failure(exception) => completeWithEither(-\/(exception))}}

  def fromBody(body: String): Maybe[OpenGraphElement] = {
    val propertyMap: Map[String, List[String]] = Jsoup.parse(body).getElementsByTag("meta").iterator().toList.foldLeft(Map.empty[String, List[String]]) { (m, element) =>
      val maybeProperty = Option(element.attr("property"))
      val maybeContent = Option(element.attr("content"))
      val entry =
        for {
          property <- maybeProperty
          content <- maybeContent
        } yield (property, content)

      entry.fold(m) { case (k, v) => m |+| Map(k -> List(v))}
    }

    Maybe.fromOption {
      for {
        title <- propertyMap.get(OpenGraphKeys.ogTitle).map(_.mkString)
        ogType <- propertyMap.get(OpenGraphKeys.ogType).map(_.mkString)
        url <- propertyMap.get(OpenGraphKeys.ogUrl).map(_.mkString)
        image <- propertyMap.get(OpenGraphKeys.ogImage).map(_.mkString)
    } yield Basic(title, ogType, url, image)}
  }

}
