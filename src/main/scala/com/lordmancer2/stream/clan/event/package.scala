package com.lordmancer2.stream.clan

import com.typesafe.scalalogging.LazyLogging
import com.lordmancer2.stream.eventsourcing.eventlog._
import com.lordmancer2.stream.json._

package object event extends LazyLogging {

  implicit val clanEventSerializationContext: SerializationContext[ClanEvents.Event] =
    createSerializationContext[ClanEvents.Event](mapper)
}
