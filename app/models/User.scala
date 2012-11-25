package models

import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import play.api.Play.current
import com.mongodb.casbah.commons.TypeImports.ObjectId
import models.mongoContext._

case class User(
  screenName: String,
  profileImageUrl: String,
  token: String, secret: String,
  id: ObjectId = new ObjectId
)

object Users extends ModelCompanion[User, ObjectId] {
  val collection = mongoCollection("users")
  val dao = new SalatDAO[User, ObjectId](collection = collection) {}

  def byId(id: ObjectId): Option[User] = dao.findOne(MongoDBObject("_id" -> id))
  def byScreenName(screenName: String): Option[User] = dao.findOne(MongoDBObject("screenName" -> screenName))
}