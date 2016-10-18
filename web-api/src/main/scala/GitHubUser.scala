
import scala.io._
import org.json4s._
import org.json4s.native.JsonMethods._

object GitHubUser {

  case class User(id: Long, userName: String)

  implicit val formats = DefaultFormats

  def fetchUserFromUrl(url: String): User = {
    val jsonResponse = parse(Source.fromURL(url).mkString)
    extractUser(jsonResponse)
  }

  def extractUser(obj: JValue): User = {
    val transformedJsonResponse = obj.transformField {
      case ("login", name) => ("userName", name)
    }
    transformedJsonResponse.extract[User]
  }


  def main(args: Array[String]) {
    // Extract username from argument list
    //    val name = args.headOption.getOrElse(throw new IllegalArgumentException("Missing user name"))
    val name = "pbugnion"
    val user = fetchUserFromUrl(s"https://api.github.com/users/$name")

    println(s"** Extracted for $name:")
    println()
    println(user)

  }

}
