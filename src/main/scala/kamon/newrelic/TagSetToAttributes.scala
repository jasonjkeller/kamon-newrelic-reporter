package kamon.newrelic

import com.newrelic.telemetry.Attributes
import kamon.tag.{Tag, TagSet}

class TagSetToAttributes {
}

object TagSetToAttributes {
  def addTags(tagSeq: Seq[TagSet], attributes: Attributes = new Attributes()): Attributes = {
    tagSeq.foreach { tagset: TagSet =>
      tagset.iterator().foreach(pair => {
        val value: Any = Tag.unwrapValue(pair)
        // Maintain the type of the tag value consistent with NR Attribute types
        value match {
          case value: String => attributes.put(pair.key, value)
          case value: Long => attributes.put(pair.key, value)
          case value: Boolean => attributes.put(pair.key, value)
        }
      })
    }
    attributes
  }
}
