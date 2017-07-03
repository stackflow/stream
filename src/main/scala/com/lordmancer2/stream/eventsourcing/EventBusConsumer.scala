package com.lordmancer2.stream.eventsourcing

import com.lordmancer2.stream.eventsourcing.eventlog.Commit
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait EventBusConsumer[Event] extends LazyLogging {

  val eventBus: EntityEventBus[Event]

  def receiveUpdate(id: String, commit: Commit[Event]): Future[Unit] = {
    Future {
      commit.events foreach { event =>
        eventBus publish Envelope(commit.id, commit.revision, commit.tm, event)
      }
    }
  }
}

