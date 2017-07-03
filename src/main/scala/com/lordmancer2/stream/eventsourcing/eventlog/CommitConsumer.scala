package com.lordmancer2.stream.eventsourcing.eventlog

import com.lordmancer2.stream.kafka.{Consumer => KafkaConsumer}

trait CommitConsumer[Event] extends KafkaConsumer[Commit[Event]]
