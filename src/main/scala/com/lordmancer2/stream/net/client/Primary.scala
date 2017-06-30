package com.lordmancer2.stream.net.client

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, OverflowStrategy, QueueOfferResult}
import com.lordmancer2.stream.App
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

object Primary {

  case class HeroProfile(userId: String, heroId: String)

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

  def queueRequest(request: HttpRequest): Future[HttpResponse] = {
    val responsePromise = Promise[HttpResponse]()
    queue.offer(request -> responsePromise).flatMap {
      case QueueOfferResult.Enqueued => responsePromise.future
      case QueueOfferResult.Dropped => Future.failed(new RuntimeException("Queue overflowed. Try again later."))
      case QueueOfferResult.Failure(ex) => Future.failed(ex)
      case QueueOfferResult.QueueClosed => Future.failed(new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
    }
  }

  def requestHeroProfile(userId: String, heroId: String): Future[HttpResponse] = {
    queueRequest(HttpRequest(uri = s"/users/$userId/heroes/$heroId/profile"))
  }

}
