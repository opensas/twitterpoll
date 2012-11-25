package models

import play.api.Play.current
import com.novus.salat._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import models.mongoContext._

case class Answer(
  pollId: ObjectId,
  userId: String,
  option: Int
)

object Answers extends ModelCompanion[Answer, ObjectId] {
  val collection = mongoCollection("answers")
  val dao = new SalatDAO[Answer, ObjectId](collection = collection) {}

  def byId(id: ObjectId): Option[Answer] = dao.findOne(MongoDBObject("_id" -> id))

  def countByPoll(pollId: ObjectId): Long = {
    dao.count(MongoDBObject("pollId" -> pollId))
  }

  def countByOption(pollId: ObjectId, option: Int): Long = {
    dao.count(MongoDBObject("pollId" -> pollId, "option" -> option))
  }

  def all() = dao.find(MongoDBObject.empty).toSeq
}