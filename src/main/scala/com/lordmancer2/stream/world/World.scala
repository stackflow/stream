package com.lordmancer2.stream.world

import java.util.Date

import com.lordmancer2.stream.eventsourcing.{EntityEventBus, Envelope}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object World extends LazyLogging {

  /** Локальная шина событий мира */
  object EventBus extends EntityEventBus[WorldEvents.Event]

  def apply(): Unit = {
    WorldEventsConsumer.add { msg =>
      Future {
        EventBus publish Envelope("world", 0, new Date, msg)
      }
    }
  }

}
