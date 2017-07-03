package com.lordmancer2.stream.world

import com.lordmancer2.stream.json._

object WorldEvents {

  trait Event extends JsonSerializable

  /** Уведомление об обновлении языковых ресурсов */
  case class LocationResourcesUpdated(locationId: String) extends Event

  /** Уведомление об обновлении талантов ресурсов */
  case class TalentResourcesUpdated(raceId: String) extends Event

  /** Уведомление об обновлении каких либо других ресурсов */
  case class ResourcesUpdated(resource: String) extends Event

  /** Уведомление об проведении работ на сервере. */
  case class DontWorryBeHappy() extends Event

}