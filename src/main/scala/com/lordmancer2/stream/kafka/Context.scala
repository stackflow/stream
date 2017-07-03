package com.lordmancer2.stream.kafka

import com.lordmancer2.stream.App

object Context {

  val bootstrapServers: String = App.config.getString("kafka.bootstrap.servers")

  val topicPrefix: String = App.config.getString("kafka.topic.prefix")
}
