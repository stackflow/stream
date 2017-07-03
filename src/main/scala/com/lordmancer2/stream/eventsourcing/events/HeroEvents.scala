package com.lordmancer2.stream.eventsourcing.events

object HeroEvents {

  trait Event extends UserEvents.Event {

    val heroId: String

  }

  /** Событие увеличения уровня */
  case class LevelUpped(heroId: String,
                        value: Int) extends Event

  /** Событие об открытии сундука */
  case class ChestOpened(heroId: String,
                         chestId: Option[String],
                         gachaId: Option[String]) extends Event

  /** Очки доминирования изменились */
  case class DominanceUpdated(heroId: String,
                              dominance: Int,
                              reason: Option[String]) extends Event

}
