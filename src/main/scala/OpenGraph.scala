package opengraph

import dispatch.{Http, url}
import model.{Basic, OpenGraphElement, OpenGraphRequest}
import org.jsoup.Jsoup

import scala.util.{Failure, Success}
import scalaz.{\/-, -\/, Maybe}
import scalaz.concurrent.Task
import scala.collection.JavaConversions._


object OpenGraph {

  import scala.concurrent.ExecutionContext.Implicits.global

  def getUnsafe(request: OpenGraphRequest): Maybe[OpenGraphElement] = getTask(request).run

  def getTask(request: OpenGraphRequest): Task[Maybe[OpenGraphElement]] =
    Task.async { completeWithEither =>
      Http.apply(url(request.url)).onComplete {
        case Success(response) => completeWithEither(\/-(fromBody(response.getResponseBody)))
        case Failure(exception) => completeWithEither(-\/(exception))
      }
    }

  def fromBody(body: String): Maybe[OpenGraphElement]= {
    val propertyMap = Jsoup.parse(body).getElementsByTag("meta").iterator().toList.foldLeft(Map.empty[String, String]) { (m, element) =>
      val maybeProperty = Option(element.attr("property"))
      val maybeContent = Option(element.attr("content"))
      val entry =
        for {
          property <- maybeProperty
          content <- maybeContent
        } yield (property, content)

      entry.fold(m) { case (k, v) => m + (k -> v)}
    }

    Maybe.fromOption{
      for {
      title <- propertyMap.get("og:title")
      t <- propertyMap.get("og:type")
      url <- propertyMap.get("og:url")
      image <- propertyMap.get("og:image")
    } yield Basic(title, t, url, image)}
  }

}
