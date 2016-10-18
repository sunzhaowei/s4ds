/**
  * Created by sunzhaowei on 2016/10/16.
  */

import scala.io._
import org.json4s._
import org.json4s.native.JsonMethods._

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import scala.util._

object GitHubUserConcurrent {
  case class User(id: Long, userName: String)

  implicit val formats = DefaultFormats

  def fetchUserFromUrl(url: String): Future[User] = {
    val response = Future {Source.fromURL(url).mkString}
    val parsedResponse = response.map{r => parse(r)}
    parsedResponse.map{extractUser}
  }

  def extractUser(obj: JValue): User = {
    val transformedJsonResponse = obj.transformField {
      case ("login", name) => ("userName", name)
    }
    transformedJsonResponse.extract[User]
  }

  def main(args: Array[String]) {
//    val names = args.toList
    val names = List("pbugnion", "sunzhaowei", "syzhjjw")

    val name2User = for{
      name <- names
      user = fetchUserFromUrl(s"https://api.github.com/users/$name")
    } yield (name, user)

    name2User.foreach{ case(name, user) =>
      user.onComplete{
        case Success(u) => println(s" ** Extracted for $name: $u")
        case Failure(e) => println(s" ** Error fetching $name: $e")
      }
    }

    Await.ready(Future.sequence(name2User.map { _._2 }), 1 minute)

  }

}
