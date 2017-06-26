package com.lordmancer2.stream

import akka.http.scaladsl.Http
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

object App extends App with Boot with LazyLogging {

  val futureBinding: Future[Http.ServerBinding] = Http().bindAndHandle(route, "127.0.0.1", 8080)

  futureBinding.onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      println(s"Akka HTTP server running at ${address.getHostString}:${address.getPort}")

    case Failure(ex) =>
      println(s"Failed to bind HTTP server: ${ex.getMessage}")
      ex.fillInStackTrace()
  }

  sys.addShutdownHook({
    logger.info("Shutting down http server")
    // trigger unbinding from the port
    futureBinding.flatMap(_.unbind()).onComplete { _ =>
      logger.info("Http server stopped")
      logger.info("Shutting down actor system")
      Await.result(system.terminate(), 1.minutes) // close actor system
      logger.info("Actor system terminated")
    }
  })

}
