import model.OpenGraphRequest
import opengraph.OpenGraph

object Main {

  import scala.concurrent.ExecutionContext.Implicits.global

  def main (args: Array[String]) {

    val request = OpenGraphRequest("http://www.theguardian.com/uk")

    println(OpenGraph.getUnsafe(request))
  }
}
