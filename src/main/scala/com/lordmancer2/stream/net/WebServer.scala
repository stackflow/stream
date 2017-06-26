package com.lordmancer2.stream.net

import akka.NotUsed
import akka.actor.{ActorSystem, PoisonPill}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Success

trait WebServer {

  implicit val system: ActorSystem

  implicit val ec: ExecutionContext

  implicit val materializer: Materializer

  def handler: Flow[Message, Message, NotUsed] = {
    // new connection - new user actor
    val userActor = system.actorOf(Stream.props())

    val incomingMessages: Sink[Message, NotUsed] = {
      Flow[Message]
        .collect {
          case TextMessage.Strict(text) =>
            Future.successful(Stream.IncomingMessage(text))
          case TextMessage.Streamed(textStream) =>
            textStream.runFold("")(_ + _)
              .flatMap(text => Future.successful(Stream.IncomingMessage(text)))
        }
        .mapAsync(1)(identity)
        .to(Sink.actorRef[Stream.IncomingMessage](userActor, PoisonPill))
    }

    val outgoingMessages: Source[Message, NotUsed] =
      Source
        .actorRef[Stream.OutgoingMessage](10, OverflowStrategy.fail)
        .mapMaterializedValue { outActor =>
          // give the user actor a way to send messages out
          userActor ! Stream.Connected(outActor)
          NotUsed
        }
        .map {
          // transform domain message to web socket message
          (outMsg: Stream.OutgoingMessage) => TextMessage(outMsg.text)
        }

    // then combine both to a flow
    Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
  }

  val route: Route = {
    pathPrefix("users" / JavaUUID / "heroes" / JavaUUID) { (userUUID, heroUUID) =>
      val userId = userUUID.toString
      val heroId = heroUUID.toString

//      val poolClientFlow = Http().cachedHostConnectionPool[Promise[HttpResponse]]("akka.io")

//      onComplete(userCtx.validatedState()) {
//        case Success(userState) =>
          path("measurements") {
            get {
              handleWebSocketMessages(handler)
            }
          }
//      }
    }
  }

}
