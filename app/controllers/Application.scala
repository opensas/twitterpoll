package controllers

import play.api._
import libs.oauth.{RequestToken, OAuthCalculator}
import libs.ws.WS
import play.api.mvc._

import views._
import models._

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import org.bson.types.ObjectId

import play.api.i18n.Messages

import play.Logger
import java.net.URLEncoder

object Application extends Controller with TwitterAuth {

  /**
   * Definition of the create poll form.
   */ 
  val createPollForm = Form(
    mapping(
      "question" -> nonEmptyText,
      "answer1" -> nonEmptyText,
      "answer2" -> nonEmptyText,
      "answer3" -> nonEmptyText
    )
      ((question, answer1, answer2, answer3) => Poll(new ObjectId(), "", question, answer1, answer2, answer3))
      (poll => Some(poll.question, poll.answer1, poll.answer2, poll.answer3))
  )

  /**
   * Definition of the answer poll form.
   */ 
  val saveAnswerForm = Form(
    "answer" -> number
  )

  def myPolls() = AuthenticatedAction { implicit request =>
    val polls = Polls.byOwner(request.user.screenName)
    Ok(html.myPolls(polls)(Some(request.user)))
  }

  def pollForm() = AuthenticatedAction { implicit request =>
    Ok(html.pollForm(createPollForm)(Some(request.user)))
  }
 
  def createPoll() = AuthenticatedAction { implicit request =>
    createPollForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.pollForm(formWithErrors)(Some(request.user))),
      poll => {
        Polls.insert(poll.copy(owner = request.user.screenName))
        Redirect(routes.Application.myPolls)
          .flashing("success" -> Messages("pollCreated"))
      }
    )
  }

  def deletePoll(id: ObjectId) = AuthenticatedAction { implicit request =>
    Polls.delete(id)
    Redirect(routes.Application.myPolls).flashing("success" -> Messages("pollDeleted"))
  }

  def answerForm(id: ObjectId) = AuthenticatedAction { implicit request =>
    Polls.byId(id).map { poll =>
      Ok(html.answerForm(poll)(Some(request.user)))
    }.getOrElse(NotFound)
  }

  def saveAnswer(id: ObjectId) = AuthenticatedAction { implicit request =>
    saveAnswerForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.answerForm(Polls.byId(id).get)(Some(request.user))),
      answer => {
        Answers.insert( Answer(id, request.user.screenName, answer))
        Redirect(routes.JavaController.index)
          .flashing("success" -> Messages("answerSaved"))
      }
    )
  }

  def tweetPoll(id: ObjectId) = AuthenticatedAction { implicit request =>

    // helper function to truncate long poll texts
    def truncateQuestion(question: String) = {
      val MAX_QUESTION_LEN = 100
      question.slice(0, MAX_QUESTION_LEN) + (if (question.size > MAX_QUESTION_LEN) "…" else "")
    }

    Polls.byId(id).map { poll =>
      val pollUrl = routes.Application.answerForm(id).absoluteURL()
      val status = URLEncoder.encode("twitterPoll %s %s".format(truncateQuestion(poll.question), pollUrl), "UTF-8")

      val promise = WS.url("https://api.twitter.com/1/statuses/update.json?status=%s".format(status)).sign(
        OAuthCalculator(TwitterAuthenticator.KEY,
          RequestToken(request.user.token, request.user.secret))).post("")
          promise.await(10000).fold(
        onError => {
          Logger.error("Error sending poll to twitter")
          Redirect(routes.Application.myPolls()).flashing("error" -> "There was an error sharing your poll on Twitter")
        },
        response => {
          Logger.debug("response: " + response.body)
          Redirect(routes.Application.myPolls()).flashing("success" -> "Your poll has been tweeted")
        }
      )
    }.getOrElse(NotFound)

  }

}
