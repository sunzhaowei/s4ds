/**
  * Created by sunzhaowei on 2016/10/17.
  */
import scala.collection.mutable
import scalaj.http._
import scala.util._
import org.json4s._
import org.json4s.native.JsonMethods._

object GhubSingleThread extends App{
  val seedUser = "sunzhaowei"

  // users whose URLs need to be fetched
  val queue = mutable.Queue(seedUser)

  // Set of users that we have already fetched
  val fetchedUsers = mutable.Set.empty[String]

//  lazy val token = sys.env.get("GHTOKEN")
  lazy val token = Some("f9bd0caef2419cd42ccfc75953472c4d39e1f246")

  def fetchFollowersForUser(login: String): List[String] = {
    val unauthorizedRequest = Http(s"https://api.github.com/users/$login/followers")
    val authorizedRequest = token.map{t => unauthorizedRequest.header("Authorization", s"token $t")}

    val request = authorizedRequest.getOrElse(unauthorizedRequest)

    val response = Try {request.asString}
    val followers = interpret(login, response)
    followers
}

  private def interpret(login: String, response: Try[HttpResponse[String]]): List[String] = response match {
    case Success(r) => responseToJson(r.body) match {
      case Success(jsonResponse) => extractFollowers(jsonResponse)
      case Failure(e) => println(s"Error parsing response to JSON for $login: $e")
        List.empty[String]
    }
    case Failure(e) => println(s"Error fetching URL for $login: $e")
      List.empty[String]
  }

  private def responseToJson(responseBody: String): Try[JArray] = {
    val jvalue = Try {parse(responseBody)}
    jvalue.flatMap{
      case a: JArray => Success(a)
      case _ => Failure(new IllegalStateException("Incorrectly formatted JSON: not an array"))
    }
  }

  private def extractFollowers(followerArray: JArray): List[String] = for {
    JObject(follower) <- followerArray
    JField("login", JString(login)) <- follower
  } yield login

  // Run the crawler
  while (queue.nonEmpty) {
    val user = queue.dequeue
    println(s"Dequeued $user")
    if (!fetchedUsers(user)) {
      println(s"Fething followers for $user")
      val followers = fetchFollowersForUser(user)
      followers foreach { follower =>
        // add the follower to queue of people whose
        // followers we want to find.
        queue += follower
      }
      fetchedUsers += user
    }
  }
}
