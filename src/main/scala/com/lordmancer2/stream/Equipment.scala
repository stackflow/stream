package com.lordmancer2.stream

import java.util.Date

import com.lordmancer2.stream.json.JsonSerializable

object Equipment {

  trait Modifier extends JsonSerializable

  case class SimpleModifier(skill: String, value: Int) extends Modifier

  case class GemModifier(skill: String, value: Int) extends Modifier

  case class Thing(id: String,
                   nameId: String,
                   imageId: String,
                   templateId: Option[String],
                   regionId: Option[String],
                   raceId: Option[String],
                   classId: Option[String],
                   level: Int,
                   quality: Int,
                   durability: Int,
                   fatigue: Int,
                   improvements: Seq[Modifier],
                   dropPrice: ThingPrice,
                   craft: ThingCraft)

  case class Anvil(thing: Thing, forgeFrom: Date, forgeTo: Date)

  case class ThingPrice(resourceId: String, count: Long)

  case class ThingCraft(count: Int, resources: Seq[Resources.SimpleResource])
}
