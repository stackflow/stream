package com.lordmancer2.stream.world

import akka.actor.Props
import com.lordmancer2.stream.App
import com.lordmancer2.stream.json.mapper
import com.lordmancer2.stream.kafka.Consumer
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.concurrent.stm._
import scala.concurrent.ExecutionContext.Implicits.global

object WorldEventsConsumer extends LazyLogging {

  val handlers = Ref(Seq[WorldEvents.Event => Future[Unit]]())

  def add(handler: WorldEvents.Event => Future[Unit]): WorldEventsConsumer.type = {
    handlers.single.transform(x => x :+ handler)
    WorldEventsConsumer
  }

  App.system.actorOf(Props(new Consumer[WorldEvents.Event] {
    override val topics = Set("world")
    override val deserialize: (String) => Future[WorldEvents.Event] = { (str: String) =>
      Future {
        mapper.readValue(str, classOf[WorldEvents.Event])
      }
    }
    override val groupIdOpt = None

    override def receiveUpdate(key: String, msg: WorldEvents.Event): Future[Unit] = {
      handlers.single().foreach { handler =>
        handler(msg)
      }
      Future.successful((): Unit)
    }
  }))

}
