package com.lordmancer2.stream.net

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import com.lordmancer2.stream.clan.Clan
import com.lordmancer2.stream.clan.event.ClanEvents
import com.lordmancer2.stream.eventsourcing.Envelope
import com.lordmancer2.stream.hero.client.Primary
import com.lordmancer2.stream.json._
import com.lordmancer2.stream.world.{World, WorldEvents}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Stream {

  case class Connected(profile: Primary.HeroProfile, outgoing: ActorRef)

  case class IncomingMessage(text: String)

  case class OutgoingMessage(text: String)

  def props()(implicit token: String): Props = {
    Props(new Stream())
  }

  case object Ping

}

class Stream()(implicit token: String) extends Actor with LazyLogging {

  implicit val ec: ExecutionContext = context.dispatcher

  private var scheduler: Cancellable = _

  override def preStart(): Unit = {
    scheduler = context.system.scheduler.schedule(60.seconds, 60.seconds, self, Stream.Ping)
    World.EventBus.subscribe(self, Some("world"))
    super.preStart()
  }

  override def postStop(): Unit = {
    scheduler.cancel()
    super.postStop()
  }

  def receive: Receive = {
    case Stream.Connected(profile, outgoing) =>
      logger.debug("Connected received")
      for (clan <- profile.clan if clan.roleId.nonEmpty) {
        Clan.EventBus.subscribe(self, Some(clan.clanId))
      }
      context.become(connected(profile, outgoing))
  }

  def connected(profile: Primary.HeroProfile, outgoing: ActorRef): Receive = {
    case incoming: Stream.IncomingMessage =>
      send(outgoing, incoming.text)

    case Stream.Ping =>
      send(outgoing, "ping")

    case Envelope(_, _, _, event: WorldEvents.Event) =>
      send(outgoing, event)

    case Envelope(_, _, _, event: ClanEvents.RequestAdded) =>
      for {
        heroProfile <- Primary.requestHeroProfile(event.userId, event.heroId) // Проверяем, что профайл существует
      } yield {
        send(outgoing, ClanEvents.ClanRequestAdded(heroProfile.userId, heroProfile.heroId, heroProfile.level, heroProfile.raceId, heroProfile.name))
      }

    case Envelope(_, _, _, event: ClanEvents.Event) =>
      send(outgoing, event)
  }

  def send[T](outgoing: ActorRef, entity: T): Unit = {
    send(outgoing, mapper.writeValueAsString(entity))
  }

  def send(outgoing: ActorRef, payload: String): Unit = {
    outgoing ! Stream.OutgoingMessage(payload)
  }

}