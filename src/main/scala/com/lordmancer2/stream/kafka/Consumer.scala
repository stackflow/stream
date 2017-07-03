package com.lordmancer2.stream.kafka

import java.util.{Properties, UUID}

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Consumer {

  val timeout = 1000 // Период проверки очереди

  case object Check

}

trait Consumer[T] extends Actor with LazyLogging {

  import context.dispatcher

  val groupIdOpt: Option[String]
  val topics: Set[String]
  val deserialize: String => Future[T]

  def receiveUpdate(key: String, msg: T): Future[Unit]

  val timeout: Int = Consumer.timeout

  var consumer: Option[KafkaConsumer[String, String]] = None

  override def preStart(): Unit = {
    val groupId: String = Context.topicPrefix + groupIdOpt.getOrElse(UUID.randomUUID().toString)

    val props = new Properties
    props.put("bootstrap.servers", Context.bootstrapServers)
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("max.poll.records", "16")
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
    props.put("group.id", groupId)

    val c = new KafkaConsumer[String, String](props)
    c.subscribe(topics.map(Context.topicPrefix + _).toList.asJava)
    consumer = Some(c)

    logger.debug(s"Kafka consumer has been assigned for $topics, groupId: $groupId")
    self ! Consumer.Check
  }

  override def postStop(): Unit = {
    consumer.foreach(_.close())
  }

  def receive: PartialFunction[Any, Unit] = {
    case Consumer.Check =>
      consumer foreach { c =>
        val records = c.poll(0).asScala
        val futures = records.foldLeft(Seq[Unit => Future[Unit]]()) { (acc, record) =>
          acc :+ {
            (_: Unit) => {
              for {
                value <- deserialize(record.value())
                _ <- receiveUpdate(record.key(), value)
              } yield {}
            }
          }
        }

        if (futures.nonEmpty) {
          c.commitSync()
          futures.foldLeft(Future.successful(())) { (acc, f) => acc.flatMap(f) } onComplete {
            case Success(_) =>
              self ! Consumer.Check // Если были данные в очереди, то не ждем, читаем следующую порцию сразу.

            case Failure(t) =>
              logger.error(s"Error during kafka consumering (topics: $topics, groupId: $groupIdOpt)", t)
              context.system.scheduler.scheduleOnce(timeout.millis, self, Consumer.Check)
          }
        } else {
          context.system.scheduler.scheduleOnce(timeout.millis, self, Consumer.Check) // Если данных не было, можно и вздремнуть
        }
      }
  }

}
