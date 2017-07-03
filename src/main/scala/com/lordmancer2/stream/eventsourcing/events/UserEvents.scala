package com.lordmancer2.stream.eventsourcing.events

import com.lordmancer2.stream.json.JsonSerializable

object UserEvents {

  trait Event extends JsonSerializable

}
