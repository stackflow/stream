package com.lordmancer2.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.lordmancer2.stream.net.WebServer

import scala.concurrent.ExecutionContext

trait Boot extends WebServer {

  implicit val system = ActorSystem()

  implicit val ec: ExecutionContext = system.dispatcher

  implicit val materializer = ActorMaterializer()

}
