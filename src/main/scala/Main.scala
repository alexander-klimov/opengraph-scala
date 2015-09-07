import model.OpenGraphRequest
import opengraph.OpenGraph

object Main {

  def main (args: Array[String]) {

    val request = OpenGraphRequest("http://www.theguardian.com/uk")

    println(OpenGraph.getUnsafe(request))
  }
}
