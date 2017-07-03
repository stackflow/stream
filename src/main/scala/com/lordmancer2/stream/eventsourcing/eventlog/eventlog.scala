package com.lordmancer2.stream.eventsourcing

import java.util.Date

import com.fasterxml.jackson.databind.ObjectMapper
import com.lordmancer2.stream.json._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

package object eventlog extends LazyLogging {

  implicit val ec: ExecutionContext = implicitly[ExecutionContext]

  case class SerializationContext[Event](serialize: Commit[Event] => Future[String],
                                         deserialize: String => Future[Commit[Event]])

  case class Commit[Event](id: String,
                           revision: Long,
                           tm: Date,
                           events: Seq[Event])

  case class SerializableCommit(id: String,
                                revision: Long,
                                tm: Date,
                                events: Seq[JsonSerializable])

  def createSerializationContext[Event <: JsonSerializable](mapper: ObjectMapper): SerializationContext[Event] = {
    SerializationContext[Event](
      serialize = { commit =>
        Try {
          mapper.writeValueAsString(SerializableCommit(commit.id, commit.revision, commit.tm, commit.events.map(_.asInstanceOf[JsonSerializable])))
        } match {
          case Success(value) => Future.successful(value)
          case Failure(e) => Future.failed(e)
        }
      },
      deserialize = { str =>
        Try {
          mapper.readValue(str, classOf[SerializableCommit])
        } match {
          case Success(commit) => Future.successful(Commit(commit.id, commit.revision, commit.tm, commit.events.map(_.asInstanceOf[Event])))
          case Failure(e) => Future.failed(e)
        }
      }
    )
  }

}
