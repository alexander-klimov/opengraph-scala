package opengraph

import dispatch.{Http, url}
import model._
import org.jsoup.Jsoup

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scalaz.concurrent.Task
import scalaz.{-\/, Maybe, \/-}
import scala.collection.JavaConversions._
import scalaz._
import Scalaz._

object OpenGraph {

  def getUnsafe(request: OpenGraphRequest)(implicit executionContext: ExecutionContext): Maybe[OpenGraphElement] =
    getTask(request).run

  def getTask(request: OpenGraphRequest)(implicit executionContext: ExecutionContext): Task[Maybe[OpenGraphElement]] =
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
        title <- propertyMap.get(OpenGraphKeys.basic.ogTitle).map(_.mkString)
        ogType <- propertyMap.get(OpenGraphKeys.basic.ogType).map(_.mkString)
        url <- propertyMap.get(OpenGraphKeys.basic.ogUrl).map(_.mkString)
        image <- propertyMap.get(OpenGraphKeys.basic.ogImage).map(_.mkString)
    } yield Basic(title, ogType, url, image)}

  def metaDataFromPropertyMap(propertyMap: Map[String, List[String]]): Maybe[MetaData] =
    Maybe.fromOption {
      for {
        ogAudio <- propertyMap.get(OpenGraphKeys.metadata.ogAudio).map(_.mkString)
        ogDescription <- propertyMap.get(OpenGraphKeys.metadata.ogDescription).map(_.mkString)
        ogDeterminer <- propertyMap.get(OpenGraphKeys.metadata.ogDeterminer).map(_.mkString)
        ogLocale <- propertyMap.get(OpenGraphKeys.metadata.ogLocale).map(_.mkString)
        ogLocaleAlternate <- propertyMap.get(OpenGraphKeys.metadata.ogLocaleAlternate)
        ogSiteName <- propertyMap.get(OpenGraphKeys.metadata.ogSiteName).map(_.mkString)
        ogVideo <- propertyMap.get(OpenGraphKeys.metadata.ogVideo).map(_.mkString)
      } yield MetaData(ogAudio, ogDescription, ogDeterminer, ogLocale, ogLocaleAlternate, ogSiteName, ogVideo)}
}
