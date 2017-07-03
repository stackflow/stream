package com.lordmancer2.stream.clan

import akka.actor.Props
import com.lordmancer2.stream.App
import com.lordmancer2.stream.clan.event.ClanEvents
import com.lordmancer2.stream.eventsourcing.eventlog.CommitConsumer
import com.lordmancer2.stream.eventsourcing.{EntityEventBus, EventBusConsumer, eventlog}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future

object Clan extends LazyLogging {

  object EventBus extends EntityEventBus[ClanEvents.Event]

  def apply(): Unit = {
    // Инициирует подключение узла к шине событий пользователя.
    App.system.actorOf(Props(new CommitConsumer[ClanEvents.Event] with EventBusConsumer[ClanEvents.Event] {
      override val topics = Set("clanevents")
      override val groupIdOpt = None
      override val eventBus = Clan.EventBus
      override val deserialize: (String) => Future[eventlog.Commit[ClanEvents.Event]] = {
        event.clanEventSerializationContext.deserialize
      }
    }))
  }

}