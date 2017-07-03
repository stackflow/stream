package com.lordmancer2.stream.clan.event

import java.util.Date

import com.lordmancer2.stream.{Equipment, Resources}
import com.lordmancer2.stream.clan.State
import com.lordmancer2.stream.json._

object ClanEvents {

  trait Event extends JsonSerializable

  case class CityId(locationId: String, cityId: String)

  case class ClanCreated(title: String, description: String) extends Event

  /** Событие, что название клана изменено */
  case class TitleUpdated(title: String) extends Event

  /** Событие о регистрации осады */
  case class SiegeUpdated(cityId: CityId, startTime: Date) extends Event

  /** Событие, что описание клана изменено */
  case class DescriptionUpdated(description: String) extends Event

  case class ClanRemoved() extends Event

  case class RequestAdded(userId: String, heroId: String) extends Event

  case class RequestAccepted(userId: String, heroId: String) extends Event

  case class RequestRejected(userId: String, heroId: String) extends Event

  case class MemberUpdated(userId: String, heroId: String, roleId: String) extends Event

  case class MemberRemoved(userId: String, heroId: String) extends Event

  /** Событие, что добавлен шмот
    *
    * Поле reason может иметь следующие значения:
    * - loot
    * - store
    */
  case class ThingsAdded(things: Seq[Equipment.Thing], reason: Option[String] = None) extends Event

  case class ThingRemoved(thingId: String) extends Event

  /** Событие, что добавлены ресурсы
    *
    * Поле reason может иметь следующие значения:
    * - loot
    * - store
    * - withdraw
    */
  case class ResourcesUpdated(resources: Seq[Resources.SimpleResource], reason: Option[String] = None) extends Event

  case class ClanSettingsUpdated(settings: State.ClanSettings) extends Event

  /** Уведомление о появлении заявки в клан */
  case class ClanRequestAdded(userId: String, heroId: String, level: Int, raceId: String, name: String) extends Event

}