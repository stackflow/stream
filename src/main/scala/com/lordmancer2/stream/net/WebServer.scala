package com.lordmancer2.stream.net

import akka.NotUsed
import akka.actor.{ActorSystem, PoisonPill}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import com.lordmancer2.stream.hero.client.Primary
import com.lordmancer2.stream.hero.client.Primary.HeroProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait WebServer {

  implicit val system: ActorSystem

  implicit val ec: ExecutionContext

  implicit val materializer: Materializer

  def handler(profile: HeroProfile)(implicit token: String): Flow[Message, Message, NotUsed] = {
    // new connection - new user actor
    val stream = system.actorOf(Stream.props())

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
        .to(Sink.actorRef[Stream.IncomingMessage](stream, PoisonPill))
    }

    val outgoingMessages: Source[Message, NotUsed] =
      Source
        .actorRef[Stream.OutgoingMessage](10, OverflowStrategy.fail)
        .mapMaterializedValue { outActor =>
          // give the user actor a way to send messages out
          stream ! Stream.Connected(profile, outActor)
          NotUsed
        }
        .map {
          // transform domain message to web socket message
          (message: Stream.OutgoingMessage) => TextMessage(message.text + "!!!")
        }

    // then combine both to a flow
    Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
  }

  val route: Route = {
    pathPrefix("users" / JavaUUID / "heroes" / JavaUUID) { (userUUID, heroUUID) =>
      headerValueByName("token") { implicit token =>
        onComplete(Primary.requestHeroProfile(userUUID.toString, heroUUID.toString)) {
          case Success(profile) =>
            pathEndOrSingleSlash {
              get {
                handleWebSocketMessages(handler(profile))
              }
            }
          case Failure(ex) =>
            complete(Forbidden, s"Error: ${ex.getMessage}")
        }
      }
    }
  }

}
