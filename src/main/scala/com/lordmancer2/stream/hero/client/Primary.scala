package com.lordmancer2.stream.hero.client

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, OverflowStrategy, QueueOfferResult}
import com.lordmancer2.stream.App
import com.lordmancer2.stream.json._
import com.typesafe.config.Config

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

object Primary {

  case class Clan(clanId: String,
                  roleId: Option[String])

  case class HeroProfile(userId: String,
                         heroId: String,
                         raceId: String,
                         classId: Option[String],
                         name: String,
                         level: Int,
                         clan: Option[Clan],
                         rankId: Option[String])

  implicit val ec: ExecutionContext = App.ec

  implicit val fm: ActorMaterializer = App.materializer

  val config: Config = App.config.getConfig("mancer.client.primary")

  val host: String = config.getString("host")

  val queueSize: Int = config.getInt("queue.size")

  val poolClientFlow: Flow[(HttpRequest, Promise[HttpResponse]), (Try[HttpResponse], Promise[HttpResponse]), Http.HostConnectionPool] = {
    Http()(App.system).cachedHostConnectionPoolHttps[Promise[HttpResponse]](host)
  }

  val queue: SourceQueueWithComplete[(HttpRequest, Promise[HttpResponse])] =
    Source.queue[(HttpRequest, Promise[HttpResponse])](queueSize, OverflowStrategy.dropNew)
      .via(poolClientFlow)
      .toMat(Sink.foreach({
        case ((Success(resp), p)) => p.success(resp)
        case ((Failure(e), p)) => p.failure(e)
      }))(Keep.left)
      .run()

  private def queueRequest(request: HttpRequest): Future[HttpResponse] = {
    val responsePromise = Promise[HttpResponse]()
    queue.offer(request -> responsePromise).flatMap {
      case QueueOfferResult.Enqueued => responsePromise.future
      case QueueOfferResult.Dropped => Future.failed(new RuntimeException("Queue overflowed. Try again later."))
      case QueueOfferResult.Failure(ex) => Future.failed(ex)
      case QueueOfferResult.QueueClosed => Future.failed(new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
    }
  }

  private def queueUriRequest(uri: String)(implicit token: String): Future[String] = {
    val headers = List[HttpHeader](RawHeader("Token", token))
    val request = HttpRequest(headers = headers, uri = uri)
    for {
      response <- queueRequest(request)
      responseAsString <- response.entity.toStrict(5.seconds).map(_.data.decodeString("UTF-8"))
    } yield {
      responseAsString
    }
  }

  def requestHeroProfile(userId: String, heroId: String)(implicit token: String): Future[HeroProfile] = {
    queueUriRequest(s"/users/$userId/heroes/$heroId/profile") map { response =>
      mapper.readValue(response, classOf[HeroProfile])
    }
  }

}
