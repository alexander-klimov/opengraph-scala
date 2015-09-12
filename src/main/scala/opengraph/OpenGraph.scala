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
        case Success(response) =>
          val propertyMap = propertyMapFromBody(response.getResponseBody)
          completeWithEither(\/-(basicFromPropertyMap(propertyMap)))
        case Failure(exception) =>
          completeWithEither(-\/(exception))}}

  def propertyMapFromBody(body: String): Map[String, List[String]] = {
    Jsoup.parse(body).getElementsByTag("meta").iterator().toList.foldLeft(Map.empty[String, List[String]]) { (m, element) =>
      val maybeProperty = Option(element.attr("property"))
      val maybeContent = Option(element.attr("content"))
      val entry =
        for {
          property <- maybeProperty
          content <- maybeContent
        } yield (property, content)

      //Use semigroup append here with Map[String, List[_]] to not loose data (Eg. og:locale:alternate)
      entry.fold(m) { case (k, v) => m |+| Map(k -> List(v)) }
    }
  }

  def basicFromPropertyMap(propertyMap: Map[String, List[String]]): Maybe[OpenGraphElement] =
    Maybe.fromOption {
      for {
        title <- propertyMap.get(OpenGraphKeys.ogTitle).map(_.mkString)
        ogType <- propertyMap.get(OpenGraphKeys.ogType).map(_.mkString)
        url <- propertyMap.get(OpenGraphKeys.ogUrl).map(_.mkString)
        image <- propertyMap.get(OpenGraphKeys.ogImage).map(_.mkString)
    } yield Basic(title, ogType, url, image)}

}
