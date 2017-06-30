package com.lordmancer2.stream.net

import java.util.concurrent.Executors

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}
import com.lordmancer2.stream.net.client.Primary

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Stream {

  case class Connected(outgoing: ActorRef)

  case class IncomingMessage(text: String)

  case class OutgoingMessage(text: String)

  def props(profile: Any): Props = {
    Props(new Stream(profile))
  }

  case object Ping

}

class Stream(profile: Any) extends Actor with ActorLogging {

  implicit val ec: ExecutionContext = context.dispatcher

  private var scheduler: Cancellable = _

  override def preStart(): Unit = {
    scheduler = context.system.scheduler.schedule(60.seconds, 60.seconds, self, Stream.Ping)
    super.preStart()
  }

  override def postStop(): Unit = {
    scheduler.cancel()
    super.postStop()
  }

  def receive: Receive = {
    case Stream.Connected(outgoing) =>
      log.debug("Connected received")
      context.become(connected(outgoing))
  }

  def connected(outgoing: ActorRef): Receive = {
    case incoming: Stream.IncomingMessage =>
      outgoing ! Stream.OutgoingMessage(incoming.text)

    case Stream.Ping =>
      outgoing ! Stream.OutgoingMessage("ping")
  }

}