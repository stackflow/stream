package com.lordmancer2.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.lordmancer2.stream.clan.Clan
import com.lordmancer2.stream.net.WebServer
import com.lordmancer2.stream.world.World
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContext

trait Boot extends WebServer {

  implicit val system: ActorSystem = ActorSystem("hero-stream")

  implicit val ec: ExecutionContext = system.dispatcher

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  implicit val config: Config = ConfigFactory.load()

  World()

  Clan()

}
