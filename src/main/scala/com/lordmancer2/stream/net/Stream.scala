package com.lordmancer2.stream.net

import akka.actor.{Actor, ActorRef, Props}

object Stream {

  case class Connected(outgoing: ActorRef)

  case class IncomingMessage(text: String)

  case class OutgoingMessage(text: String)

  def props() = Props(new Stream())

}

class Stream extends Actor {

  def receive: Receive = {
    case Stream.Connected(outgoing) =>
      context.become(connected(outgoing))
  }

  def connected(outgoing: ActorRef): Receive = {
    case text: String =>
      outgoing ! Stream.OutgoingMessage(text)
  }

}