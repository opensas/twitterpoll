package models

import play.api.Play.current

import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._

import models.mongoContext._

case class Poll(
  id: ObjectId = new ObjectId,
  owner: String,
  question: String,
  answer1: String,
  answer2: String,
  answer3: String
) {
  lazy val answers = Map(1 -> answer1, 2 -> answer2, 3 -> answer3)
  lazy val counter: Long = Answers.countByPoll(id)
  def counterForOption(o: Int): Long = Answers.countByOption(id, o)
}

object Polls extends ModelCompanion[Poll, ObjectId] {
  val collection = mongoCollection("polls")
  val dao = new SalatDAO[Poll, ObjectId](collection = collection) {}

  def byId(id: ObjectId): Option[Poll] = dao.findOne(MongoDBObject("_id" -> id))

  def all() = dao.find(MongoDBObject.empty).toSeq

  def byOwner(owner: String) = dao.find( MongoDBObject("owner" -> owner)).toSeq

  def delete(id:ObjectId) {
    Answers.remove(MongoDBObject("pollId" -> id))
    Polls.remove(MongoDBObject("_id" -> id))
  }
}