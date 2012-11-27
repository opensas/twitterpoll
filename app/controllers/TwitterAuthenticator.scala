package controllers

import models.{Users, User}

import play.api.{Logger, Play}
import play.api.Play.current

import play.api.mvc._
import play.api.mvc.Results._

import play.api.libs.ws.WS

import play.api.libs.oauth.{OAuthCalculator, RequestToken, OAuth, ConsumerKey, ServiceInfo}

/**
 * Takes care of authentication with twitter
 */
object TwitterAuthenticator extends Controller {
  val KEY = ConsumerKey(
    Play.configuration.getString("twitter.consumerKey").get,
    Play.configuration.getString("twitter.consumerSecret").get
  )

  val TWITTER = OAuth(
    ServiceInfo(
      "https://api.twitter.com/oauth/request_token",
      "https://api.twitter.com/oauth/access_token",
      "https://api.twitter.com/oauth/authorize",
      KEY
    ),
    false
  )

  /**
   * Handle twitter authentication flow
   */
  def oauthFlow = Action { implicit request =>
    request.queryString.get("oauth_verifier").flatMap(_.headOption) match {
      case Some(verifier) =>
        val requestToken = tokenFromSession(request).get
        TWITTER.retrieveAccessToken(requestToken, verifier) match {
          case Right(accessToken) => {
            val user = saveOrUpdate(accessToken)
            val goTo = request.session.get("originalUrl").getOrElse(routes.Application.myPolls().absoluteURL())
            Redirect(goTo).withSession(
              session + ("user" -> user.screenName)
            )
          }
          case Left(e) => throw e
        }
      case None => {
        if ( request.queryString.get("denied").isDefined ) {
          Redirect(routes.JavaController.index())
        } else {
          val callback = routes.TwitterAuthenticator.oauthFlow().absoluteURL()
          TWITTER.retrieveRequestToken(callback) match {
            case Right(requestToken) =>
              Logger.debug("got requestToken = " + requestToken)
              Redirect(TWITTER.redirectUrl(requestToken.token)).withSession(
                session + ("token" -> requestToken.token) + ("secret" -> requestToken.secret)
              )
            case Left(e) => throw e
          }
        }
      }
    }
  }

  def tokenFromSession(request: RequestHeader): Option[RequestToken] = {
    for {
      token <- request.session.get("token")
      secret <- request.session.get("secret")
    } yield {
      RequestToken(token, secret)
    }
  }

  def saveOrUpdate(accessToken: RequestToken): User = {
    val call = WS.url("https://api.twitter.com/1/account/verify_credentials.json").sign(
      OAuthCalculator(KEY,
        RequestToken(accessToken.token, accessToken.secret))).get()
    call.await(10000).fold(
      onError => {
        throw new RuntimeException("Timed out")
      },
      response =>
      {
        val u = response.json
        val screenName = (u \ "screen_name").as[String]
        val profileImage = (u \ "profile_image_url").as[String]

        val toSave = Users.byScreenName(screenName) match {
          case Some(user) => user.copy(profileImageUrl = profileImage, token = accessToken.token, secret = accessToken.secret )
          case None => User(screenName, profileImage, accessToken.token, accessToken.secret)
        }
        Users.save(toSave)
        toSave
      }
   )
  }

  def logout = Action { request =>
    Redirect( routes.JavaController.index() ).withNewSession
  }
}

/**
 * Enhanced request with the current user information
 */
case class RequestWithUser(user: User, request: Request[AnyContent]) extends WrappedRequest(request)

trait TwitterAuth {
  def AuthenticatedAction(f: RequestWithUser => Result) = {
    Action { request =>
      userFromSession(request) match {
        case Some(user) => f(RequestWithUser(user, request))
        case None => {
          Redirect(routes.TwitterAuthenticator.oauthFlow()).withSession("originalUrl" -> request.uri)
        }
      }
    }
  }

  def userFromSession(request: Request[AnyContent]):Option[User] = {
    request.session.get("user").flatMap(screenName => Users.byScreenName(screenName))
  }
}

