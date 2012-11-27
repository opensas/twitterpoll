package models

import play.api.Play
import play.api.Play.current

import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import com.novus.salat.{TypeHintFrequency, StringTypeHintStrategy, Context}

package object mongoContext {
  implicit val context = {
    val context = new Context {
      val name = "global"
      override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.WhenNecessary, typeHint = "_t")
    }
    context.registerGlobalKeyOverride(remapThis = "id", toThisInstead = "_id")
    context.registerClassLoader(Play.classloader)
    context
  }
}
