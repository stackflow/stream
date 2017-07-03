package com.lordmancer2.stream.eventsourcing

import java.util.Date

import akka.actor.ActorRef
import akka.event.{EventBus, SubchannelClassification}
import akka.util.Subclassification

case class Envelope[Event](id: String, revision: Long, tm: Date, event: Event)

/** http://doc.akka.io/docs/akka/current/scala/event-bus.html#subchannel-classification */
class EntityEventBus[T] extends EventBus with SubchannelClassification {

  type Event = Envelope[T]

  type Classifier = Option[String]

  type Subscriber = ActorRef

  // Subclassification is an object providing `isEqual` and `isSubclass`
  // to be consumed by the other methods of this classifier
  override protected val subclassification: Subclassification[Classifier] = {
    new Subclassification[Classifier] {

      override def isEqual(x: Classifier, y: Classifier): Boolean = {
        x == y
      }

      override def isSubclass(x: Classifier, y: Classifier): Boolean = {
        x == y || y.isEmpty
      }

    }
  }

  // is used for extracting the classifier from the incoming events
  override protected def classify(event: Event): Classifier = Some(event.id)

  // will be invoked for each event for all subscribers which registered
  // themselves for the event’s classifier
  override protected def publish(event: Event, subscriber: Subscriber): Unit = {
    subscriber ! event
  }

}
